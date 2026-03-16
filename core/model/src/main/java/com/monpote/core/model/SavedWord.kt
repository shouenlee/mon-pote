package com.monpote.core.model

data class SavedWord(
    val id: Long = 0,
    val word: String,
    val translation: String,
    val example: String,
    val reinforcementEnabled: Boolean = true,
    val savedAt: Long,
)
