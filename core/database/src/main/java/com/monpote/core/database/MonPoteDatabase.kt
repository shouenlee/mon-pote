package com.monpote.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.monpote.core.database.dao.CharacterDao
import com.monpote.core.database.dao.ConversationDao
import com.monpote.core.database.dao.MessageDao
import com.monpote.core.database.entity.CharacterEntity
import com.monpote.core.database.entity.ConversationEntity
import com.monpote.core.database.entity.MessageEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE messages ADD COLUMN translation TEXT DEFAULT NULL")
    }
}

@Database(
    entities = [
        CharacterEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class MonPoteDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
