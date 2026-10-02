PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS correspondents (
  id INTEGER PRIMARY KEY AUTOINCREMENT, remote_id TEXT, owner_id TEXT, name TEXT NOT NULL,
  match TEXT NOT NULL DEFAULT '', matching_algorithm INTEGER NOT NULL DEFAULT 1, insensitive INTEGER NOT NULL DEFAULT 1,
  UNIQUE(owner_id,name)
) STRICT;
CREATE INDEX IF NOT EXISTS idx_correspondents_name ON correspondents(name);

CREATE TABLE IF NOT EXISTS tags (
  id INTEGER PRIMARY KEY AUTOINCREMENT, remote_id TEXT, owner_id TEXT, name TEXT NOT NULL,
  match TEXT NOT NULL DEFAULT '', matching_algorithm INTEGER NOT NULL DEFAULT 1, insensitive INTEGER NOT NULL DEFAULT 1,
  color TEXT NOT NULL DEFAULT '#a6cee3', parent_id INTEGER, is_inbox INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY(parent_id) REFERENCES tags(id) ON DELETE SET NULL
) STRICT;
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);
CREATE INDEX IF NOT EXISTS idx_tags_parent ON tags(parent_id);

CREATE TABLE IF NOT EXISTS document_types (
  id INTEGER PRIMARY KEY AUTOINCREMENT, remote_id TEXT, owner_id TEXT, name TEXT NOT NULL,
  match TEXT NOT NULL DEFAULT '', matching_algorithm INTEGER NOT NULL DEFAULT 1, insensitive INTEGER NOT NULL DEFAULT 1,
  UNIQUE(owner_id,name)
) STRICT;
CREATE INDEX IF NOT EXISTS idx_document_types_name ON document_types(name);

CREATE TABLE IF NOT EXISTS storage_paths (
  id INTEGER PRIMARY KEY AUTOINCREMENT, remote_id TEXT, owner_id TEXT, name TEXT NOT NULL,
  match TEXT NOT NULL DEFAULT '', matching_algorithm INTEGER NOT NULL DEFAULT 1, insensitive INTEGER NOT NULL DEFAULT 1,
  path TEXT NOT NULL, UNIQUE(owner_id,name)
) STRICT;

CREATE TABLE IF NOT EXISTS documents (
  id INTEGER PRIMARY KEY AUTOINCREMENT, remote_id TEXT, owner_id TEXT,
  correspondent_id INTEGER, storage_path_id INTEGER, document_type_id INTEGER,
  title TEXT NOT NULL DEFAULT '', content TEXT NOT NULL DEFAULT '', content_length INTEGER NOT NULL DEFAULT 0,
  mime_type TEXT NOT NULL, checksum TEXT NOT NULL UNIQUE, archive_checksum TEXT, page_count INTEGER,
  created INTEGER NOT NULL, modified INTEGER NOT NULL, added INTEGER NOT NULL,
  filename TEXT UNIQUE, archive_filename TEXT UNIQUE, original_filename TEXT,
  archive_serial_number INTEGER UNIQUE, root_document_id INTEGER, version_index INTEGER, version_label TEXT,
  expires_at INTEGER, reminder_days_before_expiry INTEGER, is_deleted INTEGER NOT NULL DEFAULT 0,
  sync_state TEXT NOT NULL DEFAULT 'SYNCED', synced_at INTEGER,
  FOREIGN KEY(correspondent_id) REFERENCES correspondents(id) ON DELETE SET NULL,
  FOREIGN KEY(storage_path_id) REFERENCES storage_paths(id) ON DELETE SET NULL,
  FOREIGN KEY(document_type_id) REFERENCES document_types(id) ON DELETE SET NULL,
  FOREIGN KEY(root_document_id) REFERENCES documents(id) ON DELETE CASCADE,
  UNIQUE(root_document_id,version_index)
) STRICT;
CREATE INDEX IF NOT EXISTS idx_documents_created ON documents(created);
CREATE INDEX IF NOT EXISTS idx_documents_modified ON documents(modified);
CREATE INDEX IF NOT EXISTS idx_documents_added ON documents(added);
CREATE INDEX IF NOT EXISTS idx_documents_owner_created ON documents(owner_id,created);
CREATE INDEX IF NOT EXISTS idx_documents_expiry ON documents(expires_at);
CREATE INDEX IF NOT EXISTS idx_documents_root_version ON documents(root_document_id,version_index);

CREATE TABLE IF NOT EXISTS document_tags (
  document_id INTEGER NOT NULL, tag_id INTEGER NOT NULL,
  PRIMARY KEY(document_id,tag_id),
  FOREIGN KEY(document_id) REFERENCES documents(id) ON DELETE CASCADE,
  FOREIGN KEY(tag_id) REFERENCES tags(id) ON DELETE CASCADE
) STRICT;

CREATE TABLE IF NOT EXISTS custom_fields (
  id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, data_type TEXT NOT NULL, extra_data_json TEXT, created INTEGER NOT NULL
) STRICT;
CREATE TABLE IF NOT EXISTS custom_field_instances (
  id INTEGER PRIMARY KEY AUTOINCREMENT, document_id INTEGER NOT NULL, field_id INTEGER NOT NULL, created INTEGER NOT NULL,
  value_text TEXT, value_bool INTEGER, value_url TEXT, value_date INTEGER, value_int INTEGER, value_float REAL,
  value_monetary TEXT, value_document_ids_json TEXT, value_select TEXT, value_long_text TEXT, is_deleted INTEGER NOT NULL DEFAULT 0,
  UNIQUE(document_id,field_id), FOREIGN KEY(document_id) REFERENCES documents(id) ON DELETE CASCADE, FOREIGN KEY(field_id) REFERENCES custom_fields(id) ON DELETE CASCADE
) STRICT;
CREATE INDEX IF NOT EXISTS idx_cf_field_date ON custom_field_instances(field_id,value_date);
CREATE INDEX IF NOT EXISTS idx_cf_field_int ON custom_field_instances(field_id,value_int);
CREATE INDEX IF NOT EXISTS idx_cf_field_float ON custom_field_instances(field_id,value_float);

CREATE VIRTUAL TABLE IF NOT EXISTS document_fts USING fts5(document_id UNINDEXED, title, content, original_filename);

CREATE TRIGGER IF NOT EXISTS documents_fts_ai AFTER INSERT ON documents BEGIN
  INSERT INTO document_fts(document_id,title,content,original_filename) VALUES(new.id,new.title,new.content,new.original_filename);
END;
CREATE TRIGGER IF NOT EXISTS documents_fts_ad AFTER DELETE ON documents BEGIN
  DELETE FROM document_fts WHERE document_id=old.id;
END;
CREATE TRIGGER IF NOT EXISTS documents_fts_au AFTER UPDATE OF title,content,original_filename ON documents BEGIN
  DELETE FROM document_fts WHERE document_id=old.id;
  INSERT INTO document_fts(document_id,title,content,original_filename) VALUES(new.id,new.title,new.content,new.original_filename);
END;
