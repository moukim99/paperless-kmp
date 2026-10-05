package com.nextstepai.paperless.database

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

fun createFtsTableAndTriggers(connection: SQLiteConnection) {
    connection.execSQL("""
        CREATE VIRTUAL TABLE IF NOT EXISTS documents_fts USING fts5(
            title, content, filename,
            content='documents', content_rowid='id', tokenize='unicode61'
        )
    """.trimIndent())
    connection.execSQL("""
        CREATE TRIGGER IF NOT EXISTS documents_fts_ai AFTER INSERT ON documents BEGIN
          INSERT INTO documents_fts(rowid, title, content, filename)
          VALUES (new.id, new.title, new.content, coalesce(new.filename, ''));
        END
    """.trimIndent())
    connection.execSQL("""
        CREATE TRIGGER IF NOT EXISTS documents_fts_ad AFTER DELETE ON documents BEGIN
          INSERT INTO documents_fts(documents_fts, rowid, title, content, filename)
          VALUES('delete', old.id, old.title, old.content, coalesce(old.filename, ''));
        END
    """.trimIndent())
    connection.execSQL("""
        CREATE TRIGGER IF NOT EXISTS documents_fts_au AFTER UPDATE OF title, content, filename ON documents BEGIN
          INSERT INTO documents_fts(documents_fts, rowid, title, content, filename)
          VALUES('delete', old.id, old.title, old.content, coalesce(old.filename, ''));
          INSERT INTO documents_fts(rowid, title, content, filename)
          VALUES (new.id, new.title, new.content, coalesce(new.filename, ''));
        END
    """.trimIndent())
    connection.execSQL("INSERT INTO documents_fts(documents_fts) VALUES('rebuild')")
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE documents ADD COLUMN serverVersion INTEGER NOT NULL DEFAULT 1")
        connection.execSQL("ALTER TABLE documents ADD COLUMN lastSyncedModified INTEGER")
        connection.execSQL("CREATE INDEX IF NOT EXISTS idx_documents_server_version ON documents(serverVersion)")
    }
}

/** FTS5 is created as a SQLite virtual table because Room 2.8.x exposes FTS3/4 annotations,
 * while the current Room KMP line does not expose @Fts5. BundledSQLiteDriver supports FTS5. */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        createFtsTableAndTriggers(connection)
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE INDEX IF NOT EXISTS idx_tags_parent_name ON tags(parentId, name)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS idx_documents_type_correspondent ON documents(documentTypeId, correspondentId)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS idx_custom_fields_name ON custom_fields(name)")
    }
}

val DATABASE_CALLBACK = object : RoomDatabase.Callback() {
    override fun onCreate(connection: SQLiteConnection) {
        createFtsTableAndTriggers(connection)
    }

    override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA journal_mode=WAL;")
        connection.execSQL("PRAGMA synchronous=NORMAL;")
    }
}

