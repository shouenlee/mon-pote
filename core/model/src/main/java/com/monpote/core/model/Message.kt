package com.monpote.core.model

enum class Role(val value: String) {
    USER("user"),
    ASSISTANT("assistant"),
}

data class Message(
    val id: Long = 0,
    val conversationId: Long,
    val content: String,
    val role: Role,
    val timestamp: Long,
    val translation: String? = null,
)
