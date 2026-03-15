# Translation Feature — Design Spec

## Overview

Long-press any AI message bubble to reveal its English translation in a slide-down card below the bubble. All AI messages are pre-translated in the background immediately after receiving them, so translations are instant on tap.

**Goal:** Let users understand AI messages without leaving the conversation flow.

## Scope

This spec covers:
- Pre-translating all AI messages via LLM in the background
- Storing translations in Room (new column on MessageEntity)
- Long-press gesture on AI bubbles to toggle translation visibility
- Slide-down translation card UI

This spec does **not** cover: individual word/phrase translation, user message translation, or translation of saved vocabulary.

## Architecture

No new modules. All changes fit within existing modules.

### Modified files

**`:core:database`**
- `entity/MessageEntity.kt` — add `translation: String?` column
- `dao/MessageDao.kt` — add `updateTranslation()` query
- `MonPoteDatabase.kt` — bump version to 2, add migration

**`:feature:chat`**
- `ChatViewModel.kt` — add `translateMessage()` called after saving AI responses, add `toggleTranslation()` for UI
- `ChatUiState.kt` — add `visibleTranslations: Set<Long>` to track which messages show translation
- `ChatScreen.kt` — pass long-press and visibility state to message list
- `components/MessageBubble.kt` — add long-press gesture on AI bubbles
- `components/TranslationCard.kt` — **new** slide-down translation card

### Unchanged modules
- `:core:network` — reuses existing `AzureOpenAiService`
- `:core:model` — no changes needed
- `:core:ui` — no changes needed
- `:feature:onboarding` — no changes needed
- `:feature:correction` — no changes needed

## Translation Flow

### Pre-translation (background)

1. AI response is received and saved to Room (existing flow in `fetchAiResponse()`)
2. Immediately after saving, launch a background coroutine to translate
3. Call `AzureOpenAiService.chatCompletion()` with a translation system prompt + the AI message content
4. Save the translation to `MessageEntity.translation` via `MessageDao.updateTranslation()`
5. UI auto-updates via Flow (translation column is now non-null)

### User interaction

1. User long-presses an AI message bubble
2. `ChatViewModel.toggleTranslation(messageId)` adds/removes the ID from `visibleTranslations` set
3. If translation is in the set → `TranslationCard` slides down below the bubble (`AnimatedVisibility` + `expandVertically`)
4. Long-press again → ID removed from set → card slides back up
5. User messages ignore long-press (no translation for messages the user wrote)

### Opening message

The AI's opening message (sent on first conversation) follows the same path — it's saved to Room, then translated in the background.

## LLM Integration

### System Prompt

```
Translate the following French text to English. Return ONLY the English translation, nothing else.
```

### Request

- System message: the translation prompt
- User message: the French text to translate
- Same deployment, same API version
- `maxCompletionTokens = 512` (translations are shorter than source)

### Parsing

The response `content` string is the English translation directly — no JSON parsing needed. Trim whitespace and save to Room.

If the API call fails or returns empty content, set `translation` to empty string `""`. The UI treats empty string as "translation unavailable" and does not show the card on long-press.

## Database Changes

### MessageEntity

Add nullable column:

| Field | Type | Notes |
|-------|------|-------|
| translation | String? | English translation. null = not yet translated, "" = failed, non-empty = ready |

### Migration (version 1 → 2)

```sql
ALTER TABLE messages ADD COLUMN translation TEXT DEFAULT NULL
```

### MessageDao

New query:
```kotlin
@Query("UPDATE messages SET translation = :translation WHERE id = :messageId")
suspend fun updateTranslation(messageId: Long, translation: String)
```

## ChatUiState Changes

Add field:

| Field | Type | Description |
|-------|------|-------------|
| visibleTranslations | Set\<Long\> | Message IDs currently showing their translation card |

Default: `emptySet()`

## ChatViewModel Changes

### toggleTranslation(messageId: Long)

Adds or removes the message ID from `visibleTranslations` set. Only works if the message has a non-null, non-empty translation.

### translateMessage(messageId: Long, content: String)

Called after saving an AI message. Fires a background coroutine:
1. Call LLM with translation prompt + content
2. Extract response content string
3. Call `messageDao.updateTranslation(messageId, translation)`
4. If exception: call `messageDao.updateTranslation(messageId, "")`

This method is private and called automatically — the user never triggers it.

## UI Specification

### MessageBubble Changes

- AI message bubbles gain a `pointerInput` with `detectTapGestures(onLongPress = ...)` that calls `onLongPress(message.id)`
- User message bubbles: no change
- The long-press triggers haptic feedback for tactile confirmation

### TranslationCard (new composable)

Appears below the AI message bubble (not inside it), slides in/out with animation.

**Layout:**
- Wraps in `AnimatedVisibility(visible, enter = expandVertically(), exit = shrinkVertically())`
- Aligned to the left, same width constraints as the AI bubble
- Small left indent to align with the bubble (accounting for the avatar space)

**Styling:**
- Background: SurfaceVariant (#2A2A4A)
- Rounded corners: 8dp
- Left margin: 44dp (28dp avatar + 16dp message padding) to align with bubble
- Right margin: 16dp
- Padding: 8dp horizontal, 6dp vertical
- "EN" badge: small text in Primary color, left side
- Translation text: italic, TextSecondary color, bodySmall style

**Loading state:**
- If `message.translation == null` (not yet translated), show "Traduction..." in italic grey
- Auto-updates when the Room Flow emits the translated message

### ChatScreen Changes

The message list items become a `Column` containing:
1. `MessageBubble` (existing)
2. `TranslationCard` (conditionally visible based on `visibleTranslations` set)

The `onLongPress` callback is passed from ChatScreen → message item → MessageBubble for AI messages only.
