import { login, refresh, logout, authenticate } from "./auth";
import { allowLoginAttempt } from "./rate-limit";
import { withSecurityHeaders, jsonError } from "./security";

export interface Env { DB: D1Database; DOCUMENTS: R2Bucket; API_TOKEN?: string; JWT_SECRET?: string }

type DocumentRow = { id:number; remote_id:string|null; server_version:number; modified:number }
type CatalogItem = { id?:number; remoteId?:string|null; name:string }
const json=(data:unknown,status=200)=>withSecurityHeaders(new Response(JSON.stringify(data),{status,headers:{"content-type":"application/json; charset=utf-8"}}))
const requireString=(v:unknown,f:string)=>{if(typeof v!=="string"||!v.length)throw new Error(`${f} is required`);return v}
const authorized=(request:Request,env:Env)=>!!env.API_TOKEN && request.headers.get("Authorization")===`Bearer ${env.API_TOKEN}`
const upsertCatalog = async (env:Env, body:any) => {
 const correspondent = body.correspondent as CatalogItem|undefined
 const documentType = body.documentType as CatalogItem|undefined
 if(correspondent?.name){
  await env.DB.prepare(`INSERT INTO correspondents(name) VALUES(?) ON CONFLICT(name) DO NOTHING`).bind(correspondent.name).run()
 }
 if(documentType?.name){
  await env.DB.prepare(`INSERT INTO document_types(name) VALUES(?) ON CONFLICT(name) DO NOTHING`).bind(documentType.name).run()
 }
 const tags = Array.isArray(body.tags) ? body.tags as CatalogItem[] : []
 for(const tag of tags){ if(tag?.name) await env.DB.prepare(`INSERT INTO tags(name) VALUES(?) ON CONFLICT(name) DO NOTHING`).bind(tag.name).run() }
 return { correspondentName: correspondent?.name ?? null, documentTypeName: documentType?.name ?? null, tagNames: tags.filter(t=>t?.name).map(t=>t.name) }
}

export default { async fetch(request:Request,env:Env):Promise<Response>{
 const url=new URL(request.url)
 try {
  if(request.method === "OPTIONS") return withSecurityHeaders(new Response(null,{status:204,headers:{"Access-Control-Allow-Origin":"*","Access-Control-Allow-Headers":"Authorization,Content-Type","Access-Control-Allow-Methods":"GET,POST,DELETE,OPTIONS"}}));
  if(!env.JWT_SECRET && !env.API_TOKEN) return jsonError("Server authentication is not configured",500);
  if(request.method === "POST" && url.pathname === "/api/auth/login") {
   const key = `login:${request.headers.get("CF-Connecting-IP") ?? "unknown"}`;
   if(!(await allowLoginAttempt(env,key))) return jsonError("Too many login attempts",429);
   return withSecurityHeaders(await login(request,{DB:env.DB,JWT_SECRET:env.JWT_SECRET!}));
  }
  if(request.method === "POST" && url.pathname === "/api/auth/refresh") return withSecurityHeaders(await refresh(request,{DB:env.DB,JWT_SECRET:env.JWT_SECRET!}));
  if(request.method === "POST" && url.pathname === "/api/auth/logout") return withSecurityHeaders(await logout(request,{DB:env.DB,JWT_SECRET:env.JWT_SECRET!}));
  if(request.method === "GET" && url.pathname === "/api/auth/me") {
   if(!env.JWT_SECRET) return jsonError("Authentication is not configured",500);
   const user = await authenticate(request,{DB:env.DB,JWT_SECRET:env.JWT_SECRET});
   return user ? json({user}) : jsonError("Unauthorized",401);
  }
  if(env.JWT_SECRET) {
   const user = await authenticate(request,{DB:env.DB,JWT_SECRET:env.JWT_SECRET});
   if(!user && !authorized(request,env)) return jsonError("Unauthorized",401);
  } else if(!authorized(request,env)) return jsonError("Unauthorized",401);
  if(request.method==="GET"&&url.pathname==="/api/documents"){
   const q=(url.searchParams.get("q")??"").trim(), limit=Math.min(Math.max(Number(url.searchParams.get("limit")??50),1),100)
   const stmt=q?env.DB.prepare(`SELECT d.id,d.remote_id,d.title,d.content,d.mime_type,d.created,d.modified,d.expires_at,d.page_count,d.filename,d.original_filename FROM document_fts f JOIN documents d ON d.id=CAST(f.document_id AS INTEGER) WHERE document_fts MATCH ? AND d.is_deleted=0 ORDER BY rank LIMIT ?`).bind(q,limit):env.DB.prepare(`SELECT id,remote_id,title,content,mime_type,created,modified,expires_at,page_count,filename,original_filename FROM documents WHERE is_deleted=0 ORDER BY created DESC,id DESC LIMIT ?`).bind(limit)
   return json((await stmt.all()).results)
  }
  if(request.method==="POST"&&url.pathname==="/api/files"){
   const form=await request.formData(), file=form.get("file"), documentId=requireString(form.get("documentId"),"documentId")
   if(!(file instanceof File))return json({error:"file is required"},400)
   const key=`documents/${documentId}/${crypto.randomUUID()}-${file.name}`
   await env.DOCUMENTS.put(key,file.stream(),{httpMetadata:{contentType:file.type||"application/octet-stream"},customMetadata:{documentId,originalFilename:file.name}})
   return json({key,sizeBytes:file.size,mimeType:file.type||"application/octet-stream"},201)
  }
  if(request.method==="POST"&&url.pathname==="/api/documents/sync"){
   const body=await request.json<any>(), checksum=requireString(body.checksum,"checksum")
   const catalog=await upsertCatalog(env,body)
   const current=await env.DB.prepare(`SELECT id,remote_id,server_version,modified FROM documents WHERE checksum=? LIMIT 1`).bind(checksum).first<DocumentRow>()
   const baseVersion=Number(body.baseVersion??0)
   if(current && current.remote_id && baseVersion>0 && baseVersion!==current.server_version)
    return json({error:"Conflict: remote document has changed",serverVersion:current.server_version},409)
   const remoteId=current?.remote_id??(typeof body.remoteId==="string"&&body.remoteId.length?body.remoteId:crypto.randomUUID())
   const nextVersion=current?current.server_version+1:1
   await env.DB.prepare(`INSERT INTO documents(remote_id,owner_id,correspondent_id,storage_path_id,document_type_id,title,content,content_length,mime_type,checksum,archive_checksum,page_count,created,modified,added,filename,archive_filename,original_filename,archive_serial_number,root_document_id,version_index,version_label,expires_at,reminder_days_before_expiry,is_deleted,sync_state,synced_at,server_version,last_synced_modified) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,'SYNCED',?,?,?) ON CONFLICT(checksum) DO UPDATE SET remote_id=excluded.remote_id,title=excluded.title,content=excluded.content,content_length=excluded.content_length,mime_type=excluded.mime_type,modified=excluded.modified,filename=excluded.filename,original_filename=excluded.original_filename,expires_at=excluded.expires_at,reminder_days_before_expiry=excluded.reminder_days_before_expiry,sync_state='SYNCED',synced_at=excluded.synced_at,server_version=excluded.server_version,last_synced_modified=excluded.last_synced_modified`).bind(remoteId,body.ownerId??null,body.correspondentId??null,body.storagePathId??null,body.documentTypeId??null,requireString(body.title,"title"),body.content??"",Number(body.content?.length??0),requireString(body.mimeType,"mimeType"),checksum,body.archiveChecksum??null,body.pageCount??null,Number(body.created),Number(body.modified),Number(body.added),body.filename??null,body.archiveFilename??null,body.originalFilename??null,body.archiveSerialNumber??null,body.rootDocumentId??null,body.versionIndex??null,body.versionLabel??null,body.expiresAt??null,body.reminderDaysBeforeExpiry??null,Date.now(),nextVersion,Number(body.modified)).run()
   const remoteDoc=await env.DB.prepare(`SELECT id FROM documents WHERE checksum=? LIMIT 1`).bind(checksum).first<{id:number}>()
   if(remoteDoc){
    if(catalog.correspondentName) await env.DB.prepare(`UPDATE documents SET correspondent_id=(SELECT id FROM correspondents WHERE name=?) WHERE id=?`).bind(catalog.correspondentName,remoteDoc.id).run()
    if(catalog.documentTypeName) await env.DB.prepare(`UPDATE documents SET document_type_id=(SELECT id FROM document_types WHERE name=?) WHERE id=?`).bind(catalog.documentTypeName,remoteDoc.id).run()
    await env.DB.prepare(`DELETE FROM document_tags WHERE document_id=?`).bind(remoteDoc.id).run()
    for(const tagName of catalog.tagNames) await env.DB.prepare(`INSERT OR IGNORE INTO document_tags(document_id,tag_id) SELECT ?,id FROM tags WHERE name=?`).bind(remoteDoc.id,tagName).run()
   }
   if(body.r2Key) await env.DB.prepare(`INSERT INTO document_files(document_id,r2_key,size_bytes,mime_type,uploaded_at) SELECT id,?,?,?,?,? FROM documents WHERE checksum=? ON CONFLICT(document_id) DO UPDATE SET r2_key=excluded.r2_key,size_bytes=excluded.size_bytes,mime_type=excluded.mime_type,uploaded_at=excluded.uploaded_at`).bind(body.r2Key,Number(body.sizeBytes??0),body.mimeType,Date.now(),checksum).run()
   return json({remoteId, r2Key:body.r2Key??null, serverVersion:nextVersion, modified:Number(body.modified)})
  }
  if(request.method==="DELETE"&&url.pathname.startsWith("/api/documents/")){
   const remoteId=decodeURIComponent(url.pathname.slice("/api/documents/".length)); if(!remoteId)return json({error:"remoteId is required"},400)
   await env.DB.prepare(`UPDATE documents SET is_deleted=1,sync_state='SYNCED',synced_at=? ,server_version=server_version+1 WHERE remote_id=?`).bind(Date.now(),remoteId).run()
   return new Response(null,{status:204})
  }
  return jsonError("Not Found",404)
 }catch(error){return jsonError(error instanceof Error?error.message:"Internal error",500)}
}}
