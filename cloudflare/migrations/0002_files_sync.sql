PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS document_files (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  document_id INTEGER NOT NULL UNIQUE,
  r2_key TEXT UNIQUE,
  size_bytes INTEGER NOT NULL DEFAULT 0,
  mime_type TEXT NOT NULL,
  uploaded_at INTEGER,
  FOREIGN KEY(document_id) REFERENCES documents(id) ON DELETE CASCADE
) STRICT;

CREATE TABLE IF NOT EXISTS sync_operations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  document_id INTEGER NOT NULL,
  operation TEXT NOT NULL CHECK(operation IN ('UPLOAD','UPDATE','DELETE')),
  state TEXT NOT NULL DEFAULT 'PENDING',
  attempt_count INTEGER NOT NULL DEFAULT 0,
  last_error TEXT,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  FOREIGN KEY(document_id) REFERENCES documents(id) ON DELETE CASCADE
) STRICT;
CREATE INDEX IF NOT EXISTS idx_sync_operations_pending ON sync_operations(state,created_at);
CREATE INDEX IF NOT EXISTS idx_sync_operations_document ON sync_operations(document_id);
