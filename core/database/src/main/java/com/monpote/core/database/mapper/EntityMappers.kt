package com.monpote.core.database.mapper

import com.monpote.core.database.entity.CharacterEntity
import com.monpote.core.database.entity.ConversationEntity
import com.monpote.core.database.entity.MessageEntity
import com.monpote.core.database.entity.SavedWordEntity
import com.monpote.core.model.Character
import com.monpote.core.model.Conversation
import com.monpote.core.model.Message
import com.monpote.core.model.Role
import com.monpote.core.model.SavedWord

fun CharacterEntity.toDomain() = Character(
    id = id, name = name, tag = tag, description = description,
    location = location, systemPrompt = systemPrompt, color = color,
)

fun Character.toEntity() = CharacterEntity(
    id = id, name = name, tag = tag, description = description,
    location = location, systemPrompt = systemPrompt, color = color,
)

fun ConversationEntity.toDomain() = Conversation(
    id = id, characterId = characterId, title = title,
    createdAt = createdAt, lastMessageAt = lastMessageAt,
)

fun Conversation.toEntity() = ConversationEntity(
    id = id, characterId = characterId, title = title,
    createdAt = createdAt, lastMessageAt = lastMessageAt,
)

fun MessageEntity.toDomain() = Message(
    id = id, conversationId = conversationId, content = content,
    role = Role.entries.first { it.value == role }, timestamp = timestamp,
    translation = translation,
)

fun Message.toEntity() = MessageEntity(
    id = id, conversationId = conversationId, content = content,
    role = role.value, timestamp = timestamp,
    translation = translation,
)

fun SavedWordEntity.toDomain() = SavedWord(
    id = id, word = word, translation = translation,
    example = example, reinforcementEnabled = reinforcementEnabled,
    savedAt = savedAt,
)

fun SavedWord.toEntity() = SavedWordEntity(
    id = id, word = word, translation = translation,
    example = example, reinforcementEnabled = reinforcementEnabled,
    savedAt = savedAt,
)
