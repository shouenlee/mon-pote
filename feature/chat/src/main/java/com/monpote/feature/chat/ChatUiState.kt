package com.monpote.feature.chat

import com.monpote.core.model.Character
import com.monpote.core.model.Message

data class ChatUiState(
    val character: Character? = null,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
