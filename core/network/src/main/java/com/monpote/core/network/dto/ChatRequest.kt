package com.monpote.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val messages: List<ChatMessage>,
    val temperature: Double = 0.9,
    @Json(name = "max_completion_tokens") val maxCompletionTokens: Int = 512,
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val role: String,
    val content: String? = null,
)
