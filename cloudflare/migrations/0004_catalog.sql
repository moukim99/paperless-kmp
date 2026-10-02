-- Phase 6: local/remote catalog support. These tables mirror Paperless concepts.
CREATE TABLE IF NOT EXISTS correspondents (
  id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, match TEXT NOT NULL DEFAULT '', matching_algorithm INTEGER NOT NULL DEFAULT 1, is_insensitive INTEGER NOT NULL DEFAULT 1
);
CREATE TABLE IF NOT EXISTS tags (
  id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, color TEXT NOT NULL DEFAULT '#a6cee3', parent_id INTEGER, is_inbox INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS document_types (
  id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, match TEXT NOT NULL DEFAULT '', matching_algorithm INTEGER NOT NULL DEFAULT 1, is_insensitive INTEGER NOT NULL DEFAULT 1
);
CREATE TABLE IF NOT EXISTS custom_fields (
  id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, data_type TEXT NOT NULL DEFAULT 'STRING', extra_data_json TEXT
);
CREATE TABLE IF NOT EXISTS document_tags (
  document_id INTEGER NOT NULL, tag_id INTEGER NOT NULL, PRIMARY KEY(document_id, tag_id)
);
CREATE TABLE IF NOT EXISTS custom_field_values (
  id INTEGER PRIMARY KEY AUTOINCREMENT, document_id INTEGER NOT NULL, field_id INTEGER NOT NULL, value_text TEXT, value_bool INTEGER, value_date INTEGER, value_int INTEGER, value_float REAL, value_monetary TEXT, value_document_ids_json TEXT, value_select TEXT, value_long_text TEXT, UNIQUE(document_id, field_id)
);
CREATE INDEX IF NOT EXISTS idx_remote_documents_correspondent ON documents(correspondent_id);
CREATE INDEX IF NOT EXISTS idx_remote_documents_type ON documents(document_type_id);
CREATE INDEX IF NOT EXISTS idx_remote_document_tags_tag ON document_tags(tag_id);
