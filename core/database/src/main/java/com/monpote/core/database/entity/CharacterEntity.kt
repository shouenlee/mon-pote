package com.monpote.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tag: String,
    val description: String,
    val location: String,
    val systemPrompt: String,
    val color: Long,
)
