# Vocabulary Builder — Design Spec

## Overview

Save words and phrases from AI messages by double-tapping a bubble to enter word selection mode, then browse saved vocabulary in a dedicated library screen. Each saved word gets an LLM-generated English translation and example sentence.

**Goal:** Build a personal vocabulary library from natural conversation, making it easy to save and review words encountered while chatting.

## Scope

This spec covers:
- Double-tap gesture on AI bubbles to enter word selection mode
- Bubble expand animation into word selection view
- Word pills with drag-to-select for phrases
- LLM call to generate translation + example for each saved word
- Saved words Room table with migration
- Vocabulary library screen (browse, search, delete, reinforcement toggle)

This spec does **not** cover: natural reinforcement (AI using saved words in conversation), auto-saving misspelled words from corrections, or word categories/tags.

## Architecture

### New Module: `:feature:vocabulary`

The library screen and its ViewModel. Depends on `:core:database`, `:core:model`, `:core:ui`.

**Files:**
- `VocabularyScreen.kt` — library screen composable
- `VocabularyViewModel.kt` — load, search, delete, toggle reinforcement

### Modified Module: `:feature:chat`

Double-tap gesture, word selection overlay, save flow.

**Modified files:**
- `ChatViewModel.kt` — add `saveWords()` method, LLM call for definitions
- `ChatScreen.kt` — add double-tap handler, word selection overlay state
- `components/MessageBubble.kt` — add double-tap gesture (distinct from long-press)

**New files:**
- `components/WordSelectionOverlay.kt` — expanded bubble with draggable word pills

### Modified Module: `:core:database`

New `SavedWord` entity and DAO, migration v2→v3.

**Modified files:**
- `MonPoteDatabase.kt` — add SavedWordEntity, bump version to 3, add migration
- `di/DatabaseModule.kt` — provide SavedWordDao

**New files:**
- `entity/SavedWordEntity.kt`
- `dao/SavedWordDao.kt`

### Modified Module: `:core:model`

New domain model.

**New files:**
- `SavedWord.kt`

### Modified Module: `:app`

- `build.gradle.kts` — add `:feature:vocabulary` dependency
- `navigation/NavGraph.kt` — add vocabulary route

### Unchanged Modules

- `:core:network` — reuses existing `AzureOpenAiService`
- `:core:ui` — no changes
- `:feature:onboarding` — no changes
- `:feature:correction` — no changes

## Data Model

### SavedWord

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK, auto-generated |
| word | String | French word or phrase |
| translation | String | English translation |
| example | String | Example sentence in French |
| reinforcementEnabled | Boolean | Default true. Toggle in library. |
| savedAt | Long | Epoch millis |

### Room Entity

```kotlin
@Entity(tableName = "saved_words")
data class SavedWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val translation: String,
    val example: String,
    val reinforcementEnabled: Boolean = true,
    val savedAt: Long,
)
```

### Migration v2→v3

```sql
CREATE TABLE IF NOT EXISTS saved_words (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    word TEXT NOT NULL,
    translation TEXT NOT NULL,
    example TEXT NOT NULL,
    reinforcementEnabled INTEGER NOT NULL DEFAULT 1,
    savedAt INTEGER NOT NULL
)
```

### SavedWordDao

```kotlin
@Dao
interface SavedWordDao {
    @Query("SELECT * FROM saved_words ORDER BY savedAt DESC")
    fun getAllWords(): Flow<List<SavedWordEntity>>

    @Query("SELECT * FROM saved_words WHERE word LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%'")
    fun searchWords(query: String): Flow<List<SavedWordEntity>>

    @Insert
    suspend fun insert(word: SavedWordEntity): Long

    @Query("DELETE FROM saved_words WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE saved_words SET reinforcementEnabled = :enabled WHERE id = :id")
    suspend fun updateReinforcement(id: Long, enabled: Boolean)
}
```

## Word Selection Flow

### Trigger: Double-Tap AI Bubble

1. User double-taps an AI message bubble
2. Bubble does a **pulse** animation (scale 1.0 → 1.04 → 1.0, 200ms ease-out). Distinct from translation's squish-bounce.
3. Haptic feedback fires
4. Bubble **expands in-place** using `animateContentSize()` with spring physics. Surrounding messages fade to 30% opacity.
5. The bubble transforms into the word selection view

### Double-Tap vs Long-Press Coexistence

Both gestures live on the same AI bubble Surface via `pointerInput`:
- `detectTapGestures(onDoubleTap = ..., onLongPress = ...)`

Compose's `detectTapGestures` supports both simultaneously — it waits a short period after the first tap to distinguish single-tap, double-tap, and long-press.

### Word Selection View

The expanded bubble contains:

**Header:**
- Small "Sélectionne les mots" label in TextMuted
- Close button (✕) in top-right

**Word pills area:**
- The message text is split into individual words
- Each word is a pill/chip with rounded corners (10dp), SurfaceVariant background
- Pills flow in a wrap layout (FlowRow)
- **Drag to select**: user touches a word and drags across adjacent words — all words in the drag path get selected as a single phrase
- Selected pills: Primary background with white text + small checkmark
- Deselecting: tap a selected pill to deselect it. If it's part of a phrase, the entire phrase deselects.

**Footer:**
- "Sauvegarder (N)" button — Primary color, shows count of selections. Disabled when nothing selected.
- Button uses spring press animation (same as send button)

### Text Splitting

Split message by whitespace. Punctuation stays attached to the preceding word (e.g., "kiffer," becomes one pill). Emojis become their own pills but are not selectable.

### Save Flow

When user taps "Sauvegarder":

1. Overlay dismisses with reverse animation (shrink back to bubble)
2. Surrounding messages fade back to 100%
3. For each selected word/phrase, launch a background LLM call:
   - System prompt: "For the French word/phrase '{word}', provide a JSON with: english_translation, example_sentence (a short example in French using this word naturally). Respond ONLY with valid JSON."
   - Parse response, create SavedWordEntity, insert into Room
4. Brief toast/snackbar: "N mot(s) sauvegardé(s)" with a green checkmark
5. If LLM call fails for a word, save it with empty translation/example — user can see it in the library as incomplete

### LLM Request for Word Definition

**System prompt:**
```
For the given French word or phrase, return a JSON object with:
- "translation": English translation
- "example": A short example sentence in French using this word naturally

Respond ONLY with valid JSON, nothing else.
```

**Expected response:**
```json
{
  "translation": "to love, to really enjoy",
  "example": "J'ai trop kiffé cette expo au Palais de Tokyo !"
}
```

## Vocabulary Library Screen

### Access

Overflow menu in ChatTopBar → "Mes mots" (new menu item, added after "Changer de pote").

### Navigation

New route: `vocabulary` in the NavGraph. Back arrow returns to the chat screen.

### Layout

**Top bar:**
- Back arrow (Primary color)
- Title: "Mes mots"
- Search icon — tapping expands an animated search field

**Search:**
- Filters in real-time as user types
- Searches both French word and English translation
- Empty search shows all words
- No results: "Aucun mot trouvé"

**Word list:**
- `LazyColumn` of word cards
- Sorted by most recently saved (newest first)

**Word card:**
- White card with 16dp rounded corners, subtle shadow (same style as chat bubbles)
- Left side:
  - French word/phrase: 15sp, SemiBold, OnBackground
  - English translation: 12sp, TextSecondary
  - Example: 11sp, italic, TextMuted
- Right side:
  - Reinforcement toggle switch (small, green when enabled)

**Interactions:**
- Swipe left on card → red delete background reveals, release to delete
- Toggle switch → updates `reinforcementEnabled` in Room
- No tap-to-expand (all info visible on the card)

**Empty state:**
- Centered illustration area
- "Pas encore de mots" in 16sp, OnBackground
- "Double-tape un message pour sauvegarder des mots" in 13sp, TextMuted

**Snackbar on delete:**
- "Mot supprimé" with "Annuler" action to undo (re-insert within 5 seconds)

### Animations

- Word cards stagger in on first load (same pattern as onboarding: 80ms delay per card, spring slide-up)
- Swipe-to-delete with `SwipeToDismiss` composable, red background slides in
- Search field expands/collapses with `AnimatedVisibility`

## ChatTopBar Changes

Add "Mes mots" menu item to the overflow dropdown, after "Changer de pote":

```kotlin
DropdownMenuItem(
    text = { Text("Mes mots") },
    onClick = {
        menuExpanded = false
        onNavigateToVocabulary()
    },
)
```

ChatTopBar gains an `onNavigateToVocabulary: () -> Unit` parameter.
ChatScreen passes this callback, which navigates to the `vocabulary` route.

## Files Summary

**New files:**
- `core/model/src/main/java/com/monpote/core/model/SavedWord.kt`
- `core/database/src/main/java/com/monpote/core/database/entity/SavedWordEntity.kt`
- `core/database/src/main/java/com/monpote/core/database/dao/SavedWordDao.kt`
- `feature/vocabulary/build.gradle.kts`
- `feature/vocabulary/src/main/java/com/monpote/feature/vocabulary/VocabularyScreen.kt`
- `feature/vocabulary/src/main/java/com/monpote/feature/vocabulary/VocabularyViewModel.kt`
- `feature/chat/src/main/java/com/monpote/feature/chat/components/WordSelectionOverlay.kt`

**Modified files:**
- `settings.gradle.kts` — add `:feature:vocabulary`
- `app/build.gradle.kts` — add `:feature:vocabulary` dependency
- `app/src/main/java/com/monpote/navigation/NavGraph.kt` — add vocabulary route
- `core/database/src/main/java/com/monpote/core/database/MonPoteDatabase.kt` — version 3, add entity + migration
- `core/database/src/main/java/com/monpote/core/database/di/DatabaseModule.kt` — provide SavedWordDao, add migration
- `core/database/src/main/java/com/monpote/core/database/mapper/EntityMappers.kt` — add SavedWord mappers
- `feature/chat/src/main/java/com/monpote/feature/chat/ChatViewModel.kt` — add saveWords(), word selection state
- `feature/chat/src/main/java/com/monpote/feature/chat/ChatUiState.kt` — add word selection state fields
- `feature/chat/src/main/java/com/monpote/feature/chat/ChatScreen.kt` — double-tap handler, overlay integration
- `feature/chat/src/main/java/com/monpote/feature/chat/components/MessageBubble.kt` — add onDoubleTap
- `feature/chat/src/main/java/com/monpote/feature/chat/components/ChatTopBar.kt` — add "Mes mots" menu item
