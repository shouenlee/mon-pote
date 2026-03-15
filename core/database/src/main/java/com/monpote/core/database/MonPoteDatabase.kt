package com.monpote.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.monpote.core.database.dao.CharacterDao
import com.monpote.core.database.dao.ConversationDao
import com.monpote.core.database.dao.MessageDao
import com.monpote.core.database.entity.CharacterEntity
import com.monpote.core.database.entity.ConversationEntity
import com.monpote.core.database.entity.MessageEntity

@Database(
    entities = [
        CharacterEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MonPoteDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
