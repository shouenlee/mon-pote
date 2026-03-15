package com.monpote.core.model

data class Conversation(
    val id: Long = 0,
    val characterId: String,
    val title: String? = null,
    val createdAt: Long,
    val lastMessageAt: Long,
)
