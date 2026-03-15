package com.monpote.core.model

data class Character(
    val id: String,
    val name: String,
    val tag: String,
    val description: String,
    val location: String,
    val systemPrompt: String,
    val color: Long,
)
