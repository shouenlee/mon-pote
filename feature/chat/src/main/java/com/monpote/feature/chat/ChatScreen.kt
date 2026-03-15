package com.monpote.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monpote.core.ui.theme.ErrorRed
import com.monpote.feature.chat.components.ChatInput
import com.monpote.feature.chat.components.ChatTopBar
import com.monpote.feature.chat.components.FeedbackPanel
import com.monpote.feature.chat.components.MessageBubble
import com.monpote.feature.chat.components.TypingIndicator

@Composable
fun ChatScreen(
    onNavigateToOnboarding: () -> Unit,
    onNewConversation: (characterId: String, conversationId: Long) -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.sendOpeningMessage()
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            uiState.character?.let { character ->
                ChatTopBar(
                    character = character,
                    onNewChat = {
                        viewModel.startNewConversation { conversationId ->
                            onNewConversation(character.id, conversationId)
                        }
                    },
                    onChangeCharacter = onNavigateToOnboarding,
                )
            }
        },
        bottomBar = {
            Column {
                FeedbackPanel(
                    correctionState = uiState.correctionState,
                    correctionResult = uiState.correctionResult,
                    onDismiss = { viewModel.dismissCorrection() },
                )

                ChatInput(
                    text = inputText,
                    isChecking = uiState.correctionState == CorrectionState.LOADING,
                    onTextChange = { newText ->
                        inputText = newText
                    },
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            viewModel.dismissCorrection()
                            inputText = ""
                        }
                    },
                    onLongPress = {
                        if (inputText.isNotBlank()) {
                            viewModel.checkCorrection(inputText)
                        }
                    },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    character = uiState.character,
                )
            }

            if (uiState.isLoading) {
                item {
                    TypingIndicator(character = uiState.character)
                }
            }

            if (uiState.error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(
                                ErrorRed.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small,
                            )
                            .clickable { viewModel.retry() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
