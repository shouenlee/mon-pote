package com.monpote.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val messages: List<ChatMessage>,
    val temperature: Double = 0.9,
    @Json(name = "max_tokens") val maxTokens: Int = 300,
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val role: String,
    val content: String,
)
