# Pre-Send Correction System — Design Spec

## Overview

A grammar and spelling checker triggered by long-pressing the chat input field. Uses the same Azure OpenAI endpoint with a grammar-checker system prompt to analyze the user's draft message. Results appear in a non-blocking feedback panel above the input area.

**Goal:** Help users learn from their mistakes before sending, without interrupting the conversation flow.

## Scope

This spec covers:
- Long-press gesture to trigger correction
- LLM-based grammar/spelling/style analysis
- Feedback panel UI showing errors with explanations
- Integration into existing ChatScreen

This spec does **not** cover: inline text highlighting, error tracking/statistics, or vocabulary builder integration.

## Architecture

### New Module: `:feature:correction`

Contains the correction service and domain models. Depends on `:core:network` and `:core:model`.

**Files:**
- `CorrectionService.kt` — orchestrates the LLM call and JSON parsing
- `CorrectionResult.kt` — domain models (`CorrectionResult`, `CorrectionError`)
- `di/CorrectionModule.kt` — Hilt module providing `CorrectionService`

### Modified Module: `:feature:chat`

Integrates the correction system into the existing chat screen.

**Modified files:**
- `ChatUiState.kt` — add correction state fields
- `ChatViewModel.kt` — add `checkCorrection()` method
- `ChatScreen.kt` — add long-press gesture on input, show feedback panel
- `components/ChatInput.kt` — add `onLongPress` callback

**New files:**
- `components/FeedbackPanel.kt` — the correction results UI

### Unchanged Modules

- `:core:network` — reuses existing `AzureOpenAiService` (same endpoint, different prompt)
- `:core:model` — no changes needed
- `:core:ui` — no changes needed
- `:core:database` — no changes needed
- `:feature:onboarding` — no changes needed

### Module Dependencies

```
:feature:chat (modified)
└── :feature:correction (new)
    └── :core:network (existing)
```

## Correction Flow

1. User types a message in ChatInput
2. User long-presses the input field
3. If input is empty, nothing happens
4. UI shows loading state: "Vérification en cours..." banner above input
5. `ChatViewModel.checkCorrection()` calls `CorrectionService.check(text)`
6. CorrectionService sends the draft text to Azure OpenAI with a grammar-checker system prompt
7. LLM returns a JSON string listing errors found
8. CorrectionService parses the JSON into a `CorrectionResult`
9. Two outcomes:
   - **No errors:** Green "Parfait ! Aucune erreur détectée" banner, auto-dismisses after 2 seconds
   - **Errors found:** Feedback panel appears with error cards
10. User reads feedback, optionally edits the message text manually
11. User taps Send whenever ready (send is never blocked)

### Panel Dismissal

- Tapping the X button dismisses the panel
- Editing the text in the input field auto-dismisses the panel (corrections are stale)
- "Parfait !" banner auto-dismisses after 2 seconds
- "Vérification indisponible" error banner dismisses on tap

### Error Handling

- If the API call fails: show "Vérification indisponible" banner in red. Send still works.
- If the LLM returns malformed JSON (not parseable): same behavior as API failure.
- The send button is never disabled by the correction system.

## LLM Integration

### System Prompt

```
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
```

### Request

Uses the existing `AzureOpenAiService.chatCompletion()` method with:
- System message: the grammar-checker prompt above
- User message: the draft text to check
- Same deployment, same API version
- No conversation history needed (single message analysis)

### Expected Response

The LLM's `content` field contains a JSON string:

```json
{
  "errors": [
    {
      "type": "orthographe",
      "original": "je suis allez",
      "correction": "je suis allé",
      "explanation": "Le participe passé avec «être» s'accorde avec le sujet masculin singulier."
    },
    {
      "type": "grammaire",
      "original": "avec mes ami",
      "correction": "avec mes amis",
      "explanation": "Le nom après «mes» doit être au pluriel."
    },
    {
      "type": "style",
      "original": "c'était bien",
      "correction": "c'était trop cool",
      "explanation": "Pour un registre plus familier et naturel entre potes."
    }
  ]
}
```

### JSON Parsing

CorrectionService extracts the `content` string from the LLM response and uses Moshi to deserialize it into `CorrectionResult`. If parsing fails (malformed JSON, unexpected structure), the service returns a failure that the ViewModel maps to the "Vérification indisponible" state.

## Domain Models

### CorrectionError

| Field | Type | Description |
|-------|------|-------------|
| type | String | "orthographe", "grammaire", or "style" |
| original | String | The incorrect text as written by user |
| correction | String | The corrected form |
| explanation | String | Brief explanation in French |

### CorrectionResult

| Field | Type | Description |
|-------|------|-------------|
| errors | List\<CorrectionError\> | List of errors found (empty if none) |

## UI Specification

### Feedback Panel

Appears between the chat message list and the input area. Does not overlay — it pushes the message list up.

**Header:**
- Error count (e.g., "3 corrections")
- Warning icon (red)
- Dismiss button (X) on the right

**Error cards** (one per error, stacked vertically, scrollable):
- Left border color indicates error type:
  - Red (#E74C3C) = orthographe
  - Orange (#E67E22) = grammaire
  - Blue (#4A90D9) = style
- Error type label in matching color
- Original text (strikethrough, grey) → corrected text (green)
- Explanation in small grey text below

**Panel styling:**
- Background: Surface (#1A1A2E)
- Top border: 2px red (#E74C3C)
- Max height: ~220dp, scrollable if more errors
- Panel has rounded top corners

### State Banners

Compact single-line banners shown in the same position as the feedback panel:

**Loading:**
- Background: SurfaceVariant (#2A2A4A)
- Text: "Vérification en cours..." with spinner icon
- Color: grey

**No errors:**
- Background: green tint (#2ECC71 at 13% opacity)
- Text: "Parfait ! Aucune erreur détectée" with checkmark
- Color: green (#2ECC71)
- Auto-dismisses after 2 seconds

**API error:**
- Background: red tint (#E74C3C at 13% opacity)
- Text: "Vérification indisponible" with X icon
- Color: red (#E74C3C)
- Dismisses on tap

## ChatUiState Changes

New fields added to `ChatUiState`:

| Field | Type | Description |
|-------|------|-------------|
| correctionState | CorrectionState | IDLE, LOADING, SUCCESS, ERROR |
| correctionResult | CorrectionResult? | Parsed errors, null when not checked |

```kotlin
enum class CorrectionState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR,
}
```

## ChatInput Changes

The `ChatInput` composable gains an `onLongPress` callback parameter. The long-press gesture is detected using `pointerInput` with `detectTapGestures(onLongPress = ...)` on the input area container (not on the TextField itself, to avoid conflicting with text selection).
