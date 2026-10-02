package com.nextstepai.paperless.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val directory = File(System.getProperty("user.home"), ".nextstepai-paperless")
    directory.mkdirs()
    return Room.databaseBuilder<AppDatabase>(name = File(directory, "paperless.db").absolutePath).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
}
