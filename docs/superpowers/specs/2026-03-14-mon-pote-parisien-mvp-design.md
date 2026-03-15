# Mon Pote Parisien — MVP Design Spec

## Overview

A native Android chat app that simulates having a Parisian pen-pal for practicing conversational French. The MVP delivers the core chat loop: pick a character, send messages, get AI responses in authentic Parisian French.

**Target user:** B1/B2 French learner who wants immersive conversation practice, not formal lessons.

## MVP Scope

The MVP includes:
- Character selection (onboarding)
- Chat conversation with one of three AI characters
- Local message persistence
- Azure OpenAI integration

The MVP does **not** include: pre-send correction, vocabulary builder, error tracking dashboard, translation features, or backup/restore.

## Technical Stack

| Component | Choice |
|-----------|--------|
| Platform | Android (min SDK 26 / Android 8.0) |
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| IDE | Android Studio |
| Architecture | Multi-module, MVVM |
| DI | Hilt |
| Database | Room (SQLite) |
| HTTP | Retrofit + OkHttp |
| JSON | Moshi |
| Async | Kotlin Coroutines + Flow |
| Navigation | Compose Navigation |
| Preferences | Preferences DataStore |
| LLM | Azure OpenAI (chat completions) |
| API Key | BuildConfig (hardcoded for solo use) |

## Module Architecture

Seven Gradle modules organized in a layered dependency graph:

```
:app
├── :feature:chat
│   ├── :core:network
│   ├── :core:database
│   ├── :core:model
│   └── :core:ui
└── :feature:onboarding
    ├── :core:database
    ├── :core:model
    └── :core:ui
```

### Module Responsibilities

- **`:app`** — App entry point (MainActivity), Compose Navigation host, Hilt application setup. Depends on both feature modules.
- **`:feature:chat`** — ChatScreen composable, ChatViewModel, message send/receive orchestration. Depends on all core modules.
- **`:feature:onboarding`** — Character selection screen and logic. Depends on core:database, core:model, core:ui.
- **`:core:network`** — Azure OpenAI API client. Retrofit service interface, OkHttp client with API key interceptor, request/response DTOs. No dependency on other core modules except core:model.
- **`:core:database`** — Room database definition, DAOs, entities, database seeding (character data). Depends on core:model.
- **`:core:model`** — Pure Kotlin domain models shared across modules. No Android dependencies. No dependencies on other modules.
- **`:core:ui`** — Compose theme (colors, typography, shapes), shared composable components (e.g., avatar). Depends on core:model.

Feature modules depend on core modules but never on each other.

## Data Model

Three Room entities:

### Character
| Field | Type | Notes |
|-------|------|-------|
| id | String | PK. "lucas", "sarah", "karim" |
| name | String | Display name |
| description | String | Short bio |
| systemPrompt | String | Full personality prompt for Azure OpenAI |
| avatarRes | Int | Drawable resource ID |

Predefined — seeded into Room on first launch via `RoomDatabase.Callback`. Not user-editable.

### Conversation
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK, auto-generated |
| characterId | String | FK → Character.id |
| title | String? | Optional, for future conversation list |
| createdAt | Long | Epoch millis |
| lastMessageAt | Long | Epoch millis, updated on each message |

One conversation per chat session. Users can have multiple conversations with the same character.

### Referential Integrity

- Conversation → Character: `onDelete = NO_ACTION` (characters are predefined and never deleted)
- Message → Conversation: `onDelete = CASCADE` (deleting a conversation removes its messages)

### Message
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK, auto-generated |
| conversationId | Long | FK → Conversation.id |
| content | String | Raw text content |
| role | String | "user" or "assistant" — maps to OpenAI API roles |
| timestamp | Long | Epoch millis |

## Chat Flow

The message send/receive cycle:

1. User types message in ChatScreen composable
2. User taps Send → `ChatViewModel.sendMessage(text)`
3. User message saved to Room immediately (optimistic UI — appears in chat right away)
4. UI shows typing indicator (animated dots in an AI-style bubble)
5. ViewModel builds the API request:
   - System message = `character.systemPrompt`
   - Message history = last 20 messages from Room (to keep token usage reasonable)
   - User's new message appended
6. POST to Azure OpenAI `chat/completions` endpoint
7. Parse response, save assistant message to Room
8. UI auto-updates via `Flow<List<Message>>` from Room DAO — typing indicator replaced with the actual response

### Error Handling

If the API call fails:
- Typing indicator is dismissed
- User message remains saved (not lost)
- Inline error banner appears below the last user message: "Impossible d'envoyer. Réessayer?"
- Tapping the banner retries the API call
- No automatic retries

### Conversation Context

The system prompt defines the character's personality, language level, and behavioral rules. The last 20 messages from the conversation are sent as history to maintain context without excessive token usage. If the conversation has fewer than 20 messages, all messages are sent.

## Screens

### 1. Onboarding — Character Selection

The first screen shown when no character has been selected.

**Layout:**
- App title with French flag
- Three character cards stacked vertically, each showing:
  - Circular avatar with character initial and signature color
  - Name + tag (e.g., "Lucas — Le Hipster")
  - Age + Paris neighborhood
  - Short personality description in French
- Footer note: "Tu pourras changer de pote plus tard dans les paramètres"

**Behavior:**
- Tapping a card saves `selectedCharacterId` to Preferences DataStore
- Creates a new Conversation in Room for this character
- Navigates to ChatScreen
- The AI character sends an opening message automatically (e.g., "Salut ! Moi c'est Lucas, je suis graphiste à Belleville. Et toi, tu fais quoi dans la vie ?")

**Character colors:**
- Lucas: purple (#7B68EE)
- Sarah: orange (#E67E22)
- Karim: green (#2ECC71)

### 2. Chat Screen

The main screen of the app — a WhatsApp/iMessage-style messaging interface.

**Components:**
- **ChatTopBar** — character avatar (colored circle with initial), name, location. Overflow menu with: "Nouveau chat" (creates a new Conversation for the same character and navigates to it) and "Changer de pote" (navigates back to the onboarding/character selection screen, clears `selectedCharacterId` from DataStore).
- **Message list** — `LazyColumn` of `MessageBubble` composables. AI messages on the left with avatar, user messages on the right. Rounded bubble corners. Timestamps below each message. Auto-scrolls to bottom on new messages.
- **TypingIndicator** — animated dots in an AI-style bubble, shown while waiting for API response.
- **ChatInput** — rounded text field + circular send button. Send button only active when text is non-empty.

**Visual style:**
- Dark theme with Paris-inspired blue accents
- AI bubbles: dark grey (#1A1A2E), rounded top-right + both bottom corners
- User bubbles: blue (#4A90D9), rounded top-left + both bottom corners
- Background: near-black (#0B0B1A)

## Navigation

Two routes using Compose Navigation:
- `onboarding` — character selection
- `chat/{characterId}` — chat screen

**Launch logic:**
1. App starts, checks Preferences DataStore for `selectedCharacterId`
2. If no character saved → navigate to `onboarding`
3. If character found → navigate directly to `chat/{characterId}`

## Azure OpenAI Integration

### API Configuration

Stored in `BuildConfig` fields (set in `local.properties`, read in `build.gradle.kts`):
- `AZURE_OPENAI_ENDPOINT` — the resource endpoint URL
- `AZURE_OPENAI_API_KEY` — the API key
- `AZURE_OPENAI_DEPLOYMENT` — the model deployment name

### Request Format

```
POST {endpoint}/openai/deployments/{deployment}/chat/completions?api-version=2024-08-01-preview

Headers:
  api-key: {api_key}
  Content-Type: application/json

Body:
{
  "messages": [
    {"role": "system", "content": "{character.systemPrompt}"},
    {"role": "user", "content": "..."},
    {"role": "assistant", "content": "..."},
    ...last 20 messages...
    {"role": "user", "content": "{new message}"}
  ]
}
```

### Generation Parameters

- `temperature`: 0.9 (high creativity for natural, varied conversation)
- `max_tokens`: 300 (keeps responses concise and chat-like, avoids essay-length replies)

### Network Layer

- Retrofit interface with a single `chatCompletion()` method
- OkHttp interceptor adds `api-key` header
- Moshi for JSON serialization/deserialization
- Coroutine-based (suspend functions)

## Character System Prompts

Each character has a detailed system prompt that defines:
- Identity (name, age, location, background, interests)
- Personality and communication style
- Language rules: use "tu", B2+ level French, no spelling/grammar errors, contemporary Parisian slang
- Behavioral rules: never switch to English, explain through context not teaching, maintain conversation thread
- Cultural context: references to current Parisian life appropriate to the character

System prompts are stored in the Room database as part of the Character entity, seeded on first launch.

## Future Extensibility (Not in scope — context only)

The multi-module architecture is designed to accommodate post-MVP features as new modules:
- **`:feature:correction`** — pre-send grammar checking (the "Vérifier" system)
- **`:feature:vocabulary`** — word saving, vocabulary library, natural reinforcement
- **`:feature:dashboard`** — error tracking, progress charts
- **`:core:translation`** — on-demand translation service

These would depend on existing core modules and be added to `:app`'s navigation graph without modifying the chat feature.
