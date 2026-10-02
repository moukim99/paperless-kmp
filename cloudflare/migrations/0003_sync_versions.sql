ALTER TABLE documents ADD COLUMN server_version INTEGER NOT NULL DEFAULT 1;
ALTER TABLE documents ADD COLUMN last_synced_modified INTEGER;
CREATE INDEX IF NOT EXISTS idx_documents_server_version ON documents(server_version);
