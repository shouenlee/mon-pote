# Pre-Send Correction System Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a grammar/spelling correction feature triggered by long-pressing the chat input, powered by the same Azure OpenAI endpoint with a grammar-checker prompt.

**Architecture:** New `:feature:correction` module with `CorrectionService` (LLM call + JSON parsing). Modifies `:feature:chat` to add long-press gesture, feedback panel UI, and correction state management. No new dependencies — reuses existing Retrofit/Moshi/Hilt from `:core:network`.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Moshi, Azure OpenAI, Coroutines

---

## File Structure

```
feature/
├── correction/                                    # NEW MODULE
│   ├── build.gradle.kts                           # Moshi + Hilt + core:network deps
│   └── src/main/java/com/monpote/feature/correction/
│       ├── CorrectionResult.kt                    # Domain models (CorrectionResult, CorrectionError)
│       └── CorrectionService.kt                   # LLM call + JSON parsing
│
└── chat/                                          # MODIFIED
    ├── build.gradle.kts                           # Add :feature:correction dependency
    └── src/main/java/com/monpote/feature/chat/
        ├── ChatUiState.kt                         # Add CorrectionState + CorrectionResult fields
        ├── ChatViewModel.kt                       # Add checkCorrection() + dismissCorrection()
        ├── ChatScreen.kt                          # Restructure bottomBar, add FeedbackPanel
        └── components/
            ├── ChatInput.kt                       # Add onLongPress callback
            └── FeedbackPanel.kt                   # NEW — correction results UI
```

Also modify:
- `settings.gradle.kts` — add `:feature:correction` module

---

## Chunk 1: Correction Module

### Task 1: Create correction module and domain models

**Files:**
- Create: `feature/correction/build.gradle.kts`
- Create: `feature/correction/src/main/java/com/monpote/feature/correction/CorrectionResult.kt`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Create feature:correction build.gradle.kts**

`feature/correction/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.monpote.feature.correction"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:model"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.moshi)
    ksp(libs.moshi.codegen)

    implementation(libs.coroutines.android)
}
```

- [ ] **Step 2: Add module to settings.gradle.kts**

Add this line after `include(":feature:chat")`:
```kotlin
include(":feature:correction")
```

- [ ] **Step 3: Create CorrectionResult domain models**

`feature/correction/src/main/java/com/monpote/feature/correction/CorrectionResult.kt`:
```kotlin
package com.monpote.feature.correction

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CorrectionResult(
    val errors: List<CorrectionError>,
)

@JsonClass(generateAdapter = true)
data class CorrectionError(
    val type: String,
    val original: String,
    val correction: String,
    val explanation: String,
)
```

- [ ] **Step 4: Commit**

```bash
git add feature/correction/ settings.gradle.kts
git commit -m "feat: add :feature:correction module with domain models"
```

### Task 2: Create CorrectionService

**Files:**
- Create: `feature/correction/src/main/java/com/monpote/feature/correction/CorrectionService.kt`

- [ ] **Step 1: Create CorrectionService**

`feature/correction/src/main/java/com/monpote/feature/correction/CorrectionService.kt`:
```kotlin
package com.monpote.feature.correction

import com.monpote.core.network.BuildConfig
import com.monpote.core.network.api.AzureOpenAiService
import com.monpote.core.network.dto.ChatMessage
import com.monpote.core.network.dto.ChatRequest
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CorrectionService @Inject constructor(
    private val openAiService: AzureOpenAiService,
    private val moshi: Moshi,
) {
    companion object {
        private val SYSTEM_PROMPT = """
Tu es un correcteur de français. Analyse le message suivant et retourne un JSON avec les erreurs trouvées.

Catégories possibles :
- "orthographe" : fautes d'orthographe, accents, accords du participe passé
- "grammaire" : genre, nombre, conjugaison, syntaxe
- "style" : choix de mots, registre, suggestions plus naturelles

Pour chaque erreur, donne :
- "type" : la catégorie
- "original" : le texte incorrect tel qu'écrit
- "correction" : la forme correcte
- "explanation" : une explication courte en français

Si le message ne contient aucune erreur, retourne {"errors": []}.

IMPORTANT : Réponds UNIQUEMENT avec le JSON valide, sans texte avant ou après.
        """.trimIndent()
    }

    suspend fun check(text: String): CorrectionResult {
        val response = openAiService.chatCompletion(
            deployment = BuildConfig.AZURE_OPENAI_DEPLOYMENT,
            request = ChatRequest(
                messages = listOf(
                    ChatMessage(role = "system", content = SYSTEM_PROMPT),
                    ChatMessage(role = "user", content = text),
                ),
            ),
        )

        val jsonString = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("No content in response")

        // Strip markdown code fences if LLM wraps JSON in ```json ... ```
        val cleanJson = jsonString
            .replace(Regex("^```json\\s*"), "")
            .replace(Regex("^```\\s*"), "")
            .replace(Regex("\\s*```$"), "")
            .trim()

        val adapter = moshi.adapter(CorrectionResult::class.java)
        return adapter.fromJson(cleanJson)
            ?: throw IllegalStateException("Failed to parse correction JSON")
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/correction/src/main/java/com/monpote/feature/correction/CorrectionService.kt
git commit -m "feat: add CorrectionService with LLM grammar-check and JSON parsing"
```

---

## Chunk 2: Chat Module Integration

### Task 3: Update ChatUiState with correction fields

**Files:**
- Modify: `feature/chat/src/main/java/com/monpote/feature/chat/ChatUiState.kt`

- [ ] **Step 1: Add CorrectionState enum and correction fields**

Replace the full file content of `feature/chat/src/main/java/com/monpote/feature/chat/ChatUiState.kt`:
```kotlin
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
```

- [ ] **Step 2: Add :feature:correction dependency to chat build.gradle.kts**

In `feature/chat/build.gradle.kts`, add after `implementation(project(":core:ui"))`:
```kotlin
    implementation(project(":feature:correction"))
```

- [ ] **Step 3: Add :feature:correction to app build.gradle.kts**

In `app/build.gradle.kts`, add after `implementation(project(":feature:chat"))`:
```kotlin
    implementation(project(":feature:correction"))
```

This is required for Hilt to discover `CorrectionService` in the dependency graph at runtime.

- [ ] **Step 4: Commit**

```bash
git add feature/chat/src/main/java/com/monpote/feature/chat/ChatUiState.kt feature/chat/build.gradle.kts app/build.gradle.kts
git commit -m "feat: add correction state fields to ChatUiState"
```

### Task 4: Add checkCorrection to ChatViewModel

**Files:**
- Modify: `feature/chat/src/main/java/com/monpote/feature/chat/ChatViewModel.kt`

- [ ] **Step 1: Add CorrectionService injection and methods**

In `ChatViewModel.kt`, make these changes:

Add import:
```kotlin
import com.monpote.feature.correction.CorrectionService
```

Add `CorrectionService` constructor parameter after `openAiService`:
```kotlin
private val correctionService: CorrectionService,
```

Add these two methods after `startNewConversation()` and before `fetchAiResponse()`:

```kotlin
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
```

- [ ] **Step 2: Commit**

```bash
git add feature/chat/src/main/java/com/monpote/feature/chat/ChatViewModel.kt
git commit -m "feat: add checkCorrection() and dismissCorrection() to ChatViewModel"
```

### Task 5: Add onLongPress to ChatInput

**Files:**
- Modify: `feature/chat/src/main/java/com/monpote/feature/chat/components/ChatInput.kt`

- [ ] **Step 1: Update ChatInput with long-press gesture and hint**

Replace the full file content of `feature/chat/src/main/java/com/monpote/feature/chat/components/ChatInput.kt`:
```kotlin
package com.monpote.feature.chat.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monpote.core.ui.theme.Primary
import com.monpote.core.ui.theme.SurfaceVariant
import com.monpote.core.ui.theme.TextMuted

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress() })
            },
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(
                            text = "Écrire un message...",
                            color = TextMuted,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceVariant,
                        unfocusedContainerColor = SurfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                )

                FloatingActionButton(
                    onClick = onSend,
                    shape = CircleShape,
                    containerColor = if (text.isNotBlank()) Primary else Primary.copy(alpha = 0.4f),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    modifier = Modifier.size(38.dp),
                ) {
                    Text(
                        text = "➤",
                        color = Color.White,
                    )
                }
            }

            if (text.isNotBlank()) {
                Text(
                    text = "Appui long pour vérifier",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 6.dp),
                )
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/chat/src/main/java/com/monpote/feature/chat/components/ChatInput.kt
git commit -m "feat: add long-press gesture and hint text to ChatInput"
```

---

## Chunk 3: Feedback Panel UI + ChatScreen Integration

### Task 6: Create FeedbackPanel composable

**Files:**
- Create: `feature/chat/src/main/java/com/monpote/feature/chat/components/FeedbackPanel.kt`

- [ ] **Step 1: Create FeedbackPanel**

`feature/chat/src/main/java/com/monpote/feature/chat/components/FeedbackPanel.kt`:
```kotlin
package com.monpote.feature.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monpote.core.ui.theme.ErrorRed
import com.monpote.core.ui.theme.Primary
import com.monpote.core.ui.theme.SurfaceVariant
import com.monpote.feature.chat.CorrectionState
import com.monpote.feature.correction.CorrectionError
import com.monpote.feature.correction.CorrectionResult
import kotlinx.coroutines.delay

private val GrammarOrange = Color(0xFFE67E22)
private val SuccessGreen = Color(0xFF2ECC71)

@Composable
fun FeedbackPanel(
    correctionState: CorrectionState,
    correctionResult: CorrectionResult?,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = correctionState != CorrectionState.IDLE,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        when (correctionState) {
            CorrectionState.LOADING -> LoadingBanner()
            CorrectionState.SUCCESS -> {
                if (correctionResult == null || correctionResult.errors.isEmpty()) {
                    SuccessBanner(onDismiss = onDismiss)
                } else {
                    ErrorPanel(
                        errors = correctionResult.errors,
                        onDismiss = onDismiss,
                    )
                }
            }
            CorrectionState.ERROR -> ErrorBanner(onDismiss = onDismiss)
            CorrectionState.IDLE -> {}
        }
    }
}

@Composable
private fun LoadingBanner() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant)
            .padding(12.dp, 10.dp),
    ) {
        CircularProgressIndicator(
            color = Color.Gray,
            strokeWidth = 2.dp,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "Vérification en cours...",
            color = Color.Gray,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun SuccessBanner(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(SuccessGreen.copy(alpha = 0.13f))
            .padding(12.dp, 10.dp),
    ) {
        Text(text = "✓", color = SuccessGreen, fontSize = 16.sp)
        Text(
            text = "Parfait !",
            color = SuccessGreen,
            fontSize = 13.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
        Text(
            text = "Aucune erreur détectée",
            color = SuccessGreen.copy(alpha = 0.7f),
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ErrorBanner(onDismiss: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(ErrorRed.copy(alpha = 0.13f))
            .clickable(onClick = onDismiss)
            .padding(12.dp, 10.dp),
    ) {
        Text(text = "✕", color = ErrorRed, fontSize = 13.sp)
        Text(
            text = "Vérification indisponible",
            color = ErrorRed,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ErrorPanel(
    errors: List<CorrectionError>,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .padding(top = 2.dp)
            .background(ErrorRed)
            .padding(top = 2.dp)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(text = "⚠", color = ErrorRed, fontSize = 14.sp)
            Text(
                text = " ${errors.size} correction${if (errors.size > 1) "s" else ""}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "✕",
                color = Color.Gray,
                fontSize = 18.sp,
                modifier = Modifier.clickable(onClick = onDismiss),
            )
        }

        // Error cards
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .heightIn(max = 220.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
        ) {
            errors.forEach { error ->
                ErrorCard(error = error)
            }
        }
    }
}

@Composable
private fun ErrorCard(error: CorrectionError) {
    val borderColor = when (error.type) {
        "orthographe" -> ErrorRed
        "grammaire" -> GrammarOrange
        "style" -> Primary
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceVariant),
    ) {
        // Left color border
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(borderColor),
        )

        Column(modifier = Modifier.padding(10.dp, 8.dp)) {
            Text(
                text = error.type.replaceFirstChar { it.uppercase() },
                color = borderColor,
                fontSize = 12.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text = error.original,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textDecoration = TextDecoration.LineThrough,
                )
                Text(
                    text = " → ",
                    color = Color.Gray,
                    fontSize = 13.sp,
                )
                Text(
                    text = error.correction,
                    color = SuccessGreen,
                    fontSize = 13.sp,
                )
            }

            Text(
                text = error.explanation,
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add feature/chat/src/main/java/com/monpote/feature/chat/components/FeedbackPanel.kt
git commit -m "feat: add FeedbackPanel composable with error cards and state banners"
```

### Task 7: Integrate correction into ChatScreen

**Files:**
- Modify: `feature/chat/src/main/java/com/monpote/feature/chat/ChatScreen.kt`

- [ ] **Step 1: Update ChatScreen with feedback panel and correction logic**

Replace the full file content of `feature/chat/src/main/java/com/monpote/feature/chat/ChatScreen.kt`:
```kotlin
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
                    onTextChange = { newText ->
                        inputText = newText
                        // Auto-dismiss corrections when user edits text (stale)
                        if (uiState.correctionState != CorrectionState.IDLE) {
                            viewModel.dismissCorrection()
                        }
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
```

- [ ] **Step 2: Commit**

```bash
git add feature/chat/src/main/java/com/monpote/feature/chat/ChatScreen.kt
git commit -m "feat: integrate FeedbackPanel into ChatScreen with correction flow"
```

### Task 8: Manual verification

- [ ] **Step 1: Build and run**

Open Android Studio, rebuild the project, and run on device/emulator.

- [ ] **Step 2: Test the correction flow**

1. Type a message with errors (e.g., "je suis allez au musée avec mes ami")
2. Long-press on the input area (not the text itself — on the padding or send button area)
3. Loading banner should appear: "Vérification en cours..."
4. After ~1-2 seconds, feedback panel should appear with error cards
5. Verify error types have correct colors (red/orange/blue)
6. Tap X to dismiss the panel
7. Send button should work at all times

- [ ] **Step 3: Test edge cases**

1. Long-press on empty input → nothing happens
2. Type a correct message ("Bonjour, comment ça va ?") → long-press → "Parfait !" banner → auto-dismisses after 2s
3. Edit text while panel is showing → panel auto-dismisses
4. Long-press while loading → no duplicate request
5. Turn off network → long-press → "Vérification indisponible" banner → send still works

- [ ] **Step 4: Final commit**

```bash
git add app/ core/ feature/ settings.gradle.kts
git commit -m "chore: finalize pre-send correction system"
```
