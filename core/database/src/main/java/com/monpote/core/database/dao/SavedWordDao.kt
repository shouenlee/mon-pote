package com.monpote.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.monpote.core.database.entity.SavedWordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedWordDao {
    @Query("SELECT * FROM saved_words ORDER BY savedAt DESC")
    fun getAllWords(): Flow<List<SavedWordEntity>>

    @Query("SELECT * FROM saved_words WHERE word LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%'")
    fun searchWords(query: String): Flow<List<SavedWordEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_words WHERE word = :word)")
    suspend fun existsByWord(word: String): Boolean

    @Insert
    suspend fun insert(word: SavedWordEntity): Long

    @Query("DELETE FROM saved_words WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE saved_words SET reinforcementEnabled = :enabled WHERE id = :id")
    suspend fun updateReinforcement(id: Long, enabled: Boolean)
}
