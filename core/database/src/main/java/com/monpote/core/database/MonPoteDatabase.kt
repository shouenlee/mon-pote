package com.monpote.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.monpote.core.database.dao.CharacterDao
import com.monpote.core.database.dao.ConversationDao
import com.monpote.core.database.dao.MessageDao
import com.monpote.core.database.dao.SavedWordDao
import com.monpote.core.database.entity.CharacterEntity
import com.monpote.core.database.entity.ConversationEntity
import com.monpote.core.database.entity.MessageEntity
import com.monpote.core.database.entity.SavedWordEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE messages ADD COLUMN translation TEXT DEFAULT NULL")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS saved_words (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                word TEXT NOT NULL,
                translation TEXT NOT NULL,
                example TEXT NOT NULL,
                reinforcementEnabled INTEGER NOT NULL DEFAULT 1,
                savedAt INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

@Database(
    entities = [
        CharacterEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        SavedWordEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class MonPoteDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun savedWordDao(): SavedWordDao
}
