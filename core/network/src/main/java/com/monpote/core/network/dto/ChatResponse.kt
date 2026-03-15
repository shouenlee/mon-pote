package com.monpote.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val choices: List<Choice>,
)

@JsonClass(generateAdapter = true)
data class Choice(
    val message: ResponseMessage,
)

@JsonClass(generateAdapter = true)
data class ResponseMessage(
    val role: String? = null,
    val content: String? = null,
    @Json(name = "reasoning_content") val reasoningContent: String? = null,
)
