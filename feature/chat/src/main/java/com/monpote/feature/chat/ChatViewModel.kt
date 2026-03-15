package com.monpote.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monpote.core.database.dao.CharacterDao
import com.monpote.core.database.dao.ConversationDao
import com.monpote.core.database.dao.MessageDao
import com.monpote.core.database.entity.ConversationEntity
import com.monpote.core.database.entity.MessageEntity
import com.monpote.core.database.mapper.toDomain
import com.monpote.core.model.Role
import com.monpote.core.network.BuildConfig
import com.monpote.core.network.api.AzureOpenAiService
import com.monpote.core.network.dto.ChatMessage
import com.monpote.core.network.dto.ChatRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val characterDao: CharacterDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val openAiService: AzureOpenAiService,
) : ViewModel() {

    private val characterId: String = checkNotNull(savedStateHandle["characterId"])

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var conversationId: Long = -1

    init {
        viewModelScope.launch {
            val entity = characterDao.getCharacterById(characterId)
            if (entity != null) {
                _uiState.update { it.copy(character = entity.toDomain()) }
            }

            val existing = conversationDao.getLatestConversation(characterId)
            if (existing != null) {
                conversationId = existing.id
            } else {
                val now = System.currentTimeMillis()
                conversationId = conversationDao.insert(
                    ConversationEntity(
                        characterId = characterId,
                        createdAt = now,
                        lastMessageAt = now,
                    )
                )
            }

            messageDao.getMessagesForConversation(conversationId).collect { entities ->
                _uiState.update { state ->
                    state.copy(messages = entities.map { it.toDomain() })
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()

            messageDao.insert(
                MessageEntity(
                    conversationId = conversationId,
                    content = text.trim(),
                    role = Role.USER.value,
                    timestamp = now,
                )
            )
            conversationDao.updateLastMessageAt(conversationId, now)

            fetchAiResponse()
        }
    }

    fun retry() {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            fetchAiResponse()
        }
    }

    fun sendOpeningMessage() {
        viewModelScope.launch {
            if (_uiState.value.messages.isNotEmpty()) return@launch
            fetchAiResponse()
        }
    }

    fun startNewConversation(onConversationCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newId = conversationDao.insert(
                ConversationEntity(
                    characterId = characterId,
                    createdAt = now,
                    lastMessageAt = now,
                )
            )
            onConversationCreated(newId)
        }
    }

    private suspend fun fetchAiResponse() {
        val character = _uiState.value.character ?: return

        _uiState.update { it.copy(isLoading = true, error = null) }

        try {
            val recentMessages = messageDao.getRecentMessages(conversationId, 20)
                .sortedBy { it.timestamp }

            val apiMessages = buildList {
                add(ChatMessage(role = "system", content = character.systemPrompt))
                recentMessages.forEach { msg ->
                    add(ChatMessage(role = msg.role, content = msg.content))
                }
            }

            val response = openAiService.chatCompletion(
                deployment = BuildConfig.AZURE_OPENAI_DEPLOYMENT,
                request = ChatRequest(messages = apiMessages),
            )

            val msg = response.choices.firstOrNull()?.message
            val assistantContent = msg?.content ?: msg?.reasoningContent
            if (assistantContent != null) {
                val now = System.currentTimeMillis()
                messageDao.insert(
                    MessageEntity(
                        conversationId = conversationId,
                        content = assistantContent,
                        role = Role.ASSISTANT.value,
                        timestamp = now,
                    )
                )
                conversationDao.updateLastMessageAt(conversationId, now)
            }

            _uiState.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Impossible d'envoyer. Réessayer ?",
                )
            }
        }
    }
}
