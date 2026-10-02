-- Phase 8: catalog names are synchronized with documents.
CREATE INDEX IF NOT EXISTS idx_correspondents_name ON correspondents(name);
CREATE INDEX IF NOT EXISTS idx_document_types_name ON document_types(name);
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);
