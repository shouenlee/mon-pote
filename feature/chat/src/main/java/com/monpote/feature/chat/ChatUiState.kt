package com.monpote.feature.chat

import com.monpote.core.model.Character
import com.monpote.core.model.Message
import com.monpote.feature.correction.CorrectionResult

enum class CorrectionState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR,
}

data class ChatUiState(
    val character: Character? = null,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val correctionState: CorrectionState = CorrectionState.IDLE,
    val correctionResult: CorrectionResult? = null,
)
