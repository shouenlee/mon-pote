package com.monpote.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.monpote.core.database.entity.ConversationEntity

@Dao
interface ConversationDao {
    @Insert
    suspend fun insert(conversation: ConversationEntity): Long

    @Query("SELECT * FROM conversations WHERE characterId = :characterId ORDER BY lastMessageAt DESC LIMIT 1")
    suspend fun getLatestConversation(characterId: String): ConversationEntity?

    @Query("UPDATE conversations SET lastMessageAt = :timestamp WHERE id = :conversationId")
    suspend fun updateLastMessageAt(conversationId: Long, timestamp: Long)
}
