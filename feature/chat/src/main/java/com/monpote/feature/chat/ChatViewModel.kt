package com.monpote.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monpote.core.database.dao.CharacterDao
import com.monpote.core.database.dao.ConversationDao
import com.monpote.core.database.dao.MessageDao
import com.monpote.core.database.dao.SavedWordDao
import com.monpote.core.database.entity.ConversationEntity
import com.monpote.core.database.entity.MessageEntity
import com.monpote.core.database.entity.SavedWordEntity
import com.monpote.core.database.mapper.toDomain
import com.monpote.core.model.Role
import com.monpote.core.network.BuildConfig
import com.monpote.core.network.api.AzureOpenAiService
import com.monpote.core.network.dto.ChatMessage
import com.monpote.core.network.dto.ChatRequest
import com.monpote.feature.correction.CorrectionService
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
    private val correctionService: CorrectionService,
    private val savedWordDao: SavedWordDao,
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

    fun checkCorrection(text: String) {
        if (text.isBlank()) return
        if (_uiState.value.correctionState == CorrectionState.LOADING) return

        viewModelScope.launch {
            _uiState.update { it.copy(correctionState = CorrectionState.LOADING, correctionResult = null) }

            try {
                val result = correctionService.check(text)
                _uiState.update {
                    it.copy(
                        correctionState = CorrectionState.SUCCESS,
                        correctionResult = result,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(correctionState = CorrectionState.ERROR, correctionResult = null) }
            }
        }
    }

    fun dismissCorrection() {
        _uiState.update { it.copy(correctionState = CorrectionState.IDLE, correctionResult = null) }
    }

    fun enterWordSelection(messageId: Long) {
        _uiState.update { it.copy(wordSelectionMessageId = messageId) }
    }

    fun exitWordSelection() {
        _uiState.update { it.copy(wordSelectionMessageId = null) }
    }

    fun saveWords(phrases: List<String>) {
        _uiState.update { it.copy(wordSelectionMessageId = null) }

        viewModelScope.launch {
            var savedCount = 0
            for (phrase in phrases) {
                if (savedWordDao.existsByWord(phrase)) continue

                try {
                    val response = openAiService.chatCompletion(
                        deployment = BuildConfig.AZURE_OPENAI_DEPLOYMENT,
                        request = ChatRequest(
                            messages = listOf(
                                ChatMessage(
                                    role = "system",
                                    content = "For the given French word or phrase, return a JSON object with:\n- \"translation\": English translation\n- \"example\": A short example sentence in French using this word naturally\n\nRespond ONLY with valid JSON, nothing else.",
                                ),
                                ChatMessage(role = "user", content = phrase),
                            ),
                            maxCompletionTokens = 4096,
                        ),
                    )

                    val jsonString = response.choices.firstOrNull()?.message?.content
                        ?.takeIf { it.isNotBlank() }
                        ?: ""

                    var translation = ""
                    var example = ""

                    if (jsonString.isNotBlank()) {
                        try {
                            val cleanJson = jsonString
                                .replace(Regex("^```json\\s*"), "")
                                .replace(Regex("^```\\s*"), "")
                                .replace(Regex("\\s*```$"), "")
                                .trim()

                            // Simple manual JSON parsing to avoid Moshi dependency in chat module
                            val translationMatch = Regex("\"translation\"\\s*:\\s*\"([^\"]+)\"").find(cleanJson)
                            val exampleMatch = Regex("\"example\"\\s*:\\s*\"([^\"]+)\"").find(cleanJson)
                            translation = translationMatch?.groupValues?.get(1) ?: ""
                            example = exampleMatch?.groupValues?.get(1) ?: ""
                        } catch (_: Exception) {}
                    }

                    savedWordDao.insert(
                        SavedWordEntity(
                            word = phrase,
                            translation = translation,
                            example = example,
                            savedAt = System.currentTimeMillis(),
                        )
                    )
                    savedCount++
                } catch (_: Exception) {
                    savedWordDao.insert(
                        SavedWordEntity(
                            word = phrase,
                            translation = "",
                            example = "",
                            savedAt = System.currentTimeMillis(),
                        )
                    )
                    savedCount++
                }
            }
        }
    }

    fun toggleTranslation(messageId: Long) {
        _uiState.update { state ->
            val current = state.visibleTranslations
            val updated = if (messageId in current) current - messageId else current + messageId
            state.copy(visibleTranslations = updated)
        }
    }

    private fun translateMessage(messageId: Long, content: String) {
        viewModelScope.launch {
            try {
                val response = openAiService.chatCompletion(
                    deployment = BuildConfig.AZURE_OPENAI_DEPLOYMENT,
                    request = ChatRequest(
                        messages = listOf(
                            ChatMessage(
                                role = "system",
                                content = "Translate the following French text to English. Return ONLY the English translation, nothing else.",
                            ),
                            ChatMessage(role = "user", content = content),
                        ),
                        maxCompletionTokens = 4096,
                    ),
                )

                val translation = response.choices.firstOrNull()?.message?.content
                    ?.takeIf { it.isNotBlank() }
                    ?: ""

                messageDao.updateTranslation(messageId, translation)
            } catch (e: Exception) {
                messageDao.updateTranslation(messageId, "")
            }
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
                val messageId = messageDao.insert(
                    MessageEntity(
                        conversationId = conversationId,
                        content = assistantContent,
                        role = Role.ASSISTANT.value,
                        timestamp = now,
                    )
                )
                conversationDao.updateLastMessageAt(conversationId, now)
                translateMessage(messageId, assistantContent)
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
