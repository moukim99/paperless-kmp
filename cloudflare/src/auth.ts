export interface AuthEnv {
  DB: D1Database;
  JWT_SECRET: string;
}

type Role = "ADMIN" | "USER";
type User = { id: string; email: string; role: Role; active: number };

const ACCESS_TTL = 15 * 60;
const REFRESH_TTL = 30 * 24 * 60 * 60;
const PBKDF2_ITERATIONS = 310_000;
const enc = new TextEncoder();

function b64url(input: ArrayBuffer | Uint8Array): string {
  const bytes = input instanceof Uint8Array ? input : new Uint8Array(input);
  let s = "";
  for (const b of bytes) s += String.fromCharCode(b);
  return btoa(s).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}
function fromB64url(s: string): Uint8Array {
  const base = s.replace(/-/g, "+").replace(/_/g, "/") + "===".slice((s.length + 3) % 4);
  const raw = atob(base); return Uint8Array.from(raw, c => c.charCodeAt(0));
}
async function hmacKey(secret: string) {
  return crypto.subtle.importKey("raw", enc.encode(secret), {name:"HMAC", hash:"SHA-256"}, false, ["sign","verify"]);
}
async function signJwt(payload: Record<string, unknown>, secret: string) {
  const header = b64url(enc.encode(JSON.stringify({alg:"HS256",typ:"JWT"})));
  const body = b64url(enc.encode(JSON.stringify(payload)));
  const data = `${header}.${body}`;
  const sig = await crypto.subtle.sign("HMAC", await hmacKey(secret), enc.encode(data));
  return `${data}.${b64url(sig)}`;
}
async function verifyJwt(token: string, secret: string): Promise<Record<string, unknown> | null> {
  try {
    const [h,p,s] = token.split(".");
    if (!h || !p || !s) return null;
    const ok = await crypto.subtle.verify("HMAC", await hmacKey(secret), fromB64url(s), enc.encode(`${h}.${p}`));
    if (!ok) return null;
    const payload = JSON.parse(new TextDecoder().decode(fromB64url(p)));
    if (payload.exp <= Math.floor(Date.now()/1000)) return null;
    return payload;
  } catch { return null; }
}
async function sha256Hex(value: string): Promise<string> {
  return [...new Uint8Array(await crypto.subtle.digest("SHA-256", enc.encode(value)))]
    .map(b => b.toString(16).padStart(2,"0")).join("");
}
async function randomToken(bytes = 32): Promise<string> {
  const b = new Uint8Array(bytes); crypto.getRandomValues(b); return b64url(b);
}
async function derivePassword(password: string, salt: Uint8Array, iterations = PBKDF2_ITERATIONS): Promise<Uint8Array> {
  const key = await crypto.subtle.importKey("raw", enc.encode(password), "PBKDF2", false, ["deriveBits"]);
  return new Uint8Array(await crypto.subtle.deriveBits({name:"PBKDF2",salt,iterations,hash:"SHA-256"}, key, 256));
}
async function hashPassword(password: string): Promise<string> {
  const salt = new Uint8Array(16); crypto.getRandomValues(salt);
  const derived = await derivePassword(password, salt);
  return `pbkdf2_sha256$${PBKDF2_ITERATIONS}$${b64url(salt)}$${b64url(derived)}`;
}
async function verifyPassword(password: string, encoded: string): Promise<boolean> {
  const [,iterations,salt,expected] = encoded.split("$");
  if (!iterations || !salt || !expected) return false;
  const derived = await derivePassword(password, fromB64url(salt), Number(iterations));
  return b64url(derived) === expected;
}

export async function issueSession(env: AuthEnv, user: User): Promise<Response> {
  const now = Math.floor(Date.now()/1000);
  const accessToken = await signJwt({sub:user.id,email:user.email,role:user.role,iat:now,exp:now+ACCESS_TTL}, env.JWT_SECRET);
  const refreshToken = await randomToken();
  const refreshId = crypto.randomUUID();
  await env.DB.prepare(`INSERT INTO refresh_tokens (id,user_id,token_hash,expires_at,created_at) VALUES (?,?,?,?,?)`)
    .bind(refreshId,user.id,await sha256Hex(refreshToken),now+REFRESH_TTL,now).run();
  return Response.json({accessToken,refreshToken,expiresAt:new Date((now+ACCESS_TTL)*1000).toISOString(),user:{id:user.id,email:user.email,role:user.role}});
}

export async function authenticate(request: Request, env: AuthEnv): Promise<User | null> {
  const value = request.headers.get("Authorization");
  if (!value?.startsWith("Bearer ")) return null;
  const payload = await verifyJwt(value.slice(7), env.JWT_SECRET);
  if (!payload?.sub) return null;
  const row = await env.DB.prepare(`SELECT id,email,role,active FROM users WHERE id=? LIMIT 1`).bind(String(payload.sub)).first<User>();
  return row && row.active ? row : null;
}

export async function login(request: Request, env: AuthEnv): Promise<Response> {
  const body = await request.json<{email?:string;password?:string}>();
  if (!body.email || !body.password) return Response.json({error:"invalid_credentials"},{status:400});
  const user = await env.DB.prepare(`SELECT id,email,password_hash,role,active FROM users WHERE lower(email)=lower(?) LIMIT 1`).bind(body.email.trim()).first<User & {password_hash:string}>();
  if (!user || !user.active || !(await verifyPassword(body.password,user.password_hash))) return Response.json({error:"invalid_credentials"},{status:401});
  return issueSession(env,user);
}

export async function refresh(request: Request, env: AuthEnv): Promise<Response> {
  const body = await request.json<{refreshToken?:string}>();
  if (!body.refreshToken) return Response.json({error:"invalid_refresh_token"},{status:401});
  const now = Math.floor(Date.now()/1000);
  const hash = await sha256Hex(body.refreshToken);
  const row = await env.DB.prepare(`SELECT r.id,r.user_id,r.expires_at,r.revoked_at,u.id,u.email,u.role,u.active FROM refresh_tokens r JOIN users u ON u.id=r.user_id WHERE r.token_hash=? LIMIT 1`).bind(hash).first<any>();
  if (!row || row.revoked_at || row.expires_at <= now || !row.active) return Response.json({error:"invalid_refresh_token"},{status:401});
  const replacement = await randomToken();
  const replacementId = crypto.randomUUID();
  const rotation = await env.DB.prepare(`UPDATE refresh_tokens SET revoked_at=?,replaced_by=? WHERE id=? AND revoked_at IS NULL AND expires_at>?`).bind(now,replacementId,row.id,now).run();
  if (rotation.meta.changes !== 1) return Response.json({error:"invalid_refresh_token"},{status:401});
  await env.DB.prepare(`INSERT INTO refresh_tokens (id,user_id,token_hash,expires_at,created_at) VALUES (?,?,?,?,?)`).bind(replacementId,row.user_id,await sha256Hex(replacement),now+REFRESH_TTL,now).run();
  const accessToken = await signJwt({sub:row.user_id,email:row.email,role:row.role,iat:now,exp:now+ACCESS_TTL}, env.JWT_SECRET);
  return Response.json({accessToken,refreshToken:replacement,expiresAt:new Date((now+ACCESS_TTL)*1000).toISOString(),user:{id:row.user_id,email:row.email,role:row.role}});
}

export async function logout(request: Request, env: AuthEnv): Promise<Response> {
  const body = await request.json<{refreshToken?:string}>();
  if (body.refreshToken) await env.DB.prepare(`UPDATE refresh_tokens SET revoked_at=? WHERE token_hash=? AND revoked_at IS NULL`).bind(Math.floor(Date.now()/1000),await sha256Hex(body.refreshToken)).run();
  return new Response(null,{status:204});
}

export { hashPassword };
