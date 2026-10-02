package com.nextstepai.paperless.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

private lateinit var appContext: Context

fun initializeDatabaseContext(context: Context) {
    appContext = context.applicationContext
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    check(::appContext.isInitialized) { "Call initializeDatabaseContext(context) before creating the database." }
    val dbFile = appContext.getDatabasePath("paperless.db")
    return Room.databaseBuilder<AppDatabase>(context = appContext, name = dbFile.absolutePath).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
}
