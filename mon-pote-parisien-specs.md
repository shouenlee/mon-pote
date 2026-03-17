# Mon Pote Parisien - App Specification Document

## Overview

**Mon Pote Parisien** is a French learning chat app that simulates having a real Parisian pen-pal. The goal is to practice conversational French through natural messaging, with intelligent correction and learning features that work alongside (not within) the conversation flow.

---

## Core Concept

- **Primary Goal**: Practice writing and conversing in French through realistic text-based conversations
- **Secondary Goal**: Learn from mistakes through pre-send corrections and feedback
- **Experience**: Like texting a real friend in Paris, not talking to a language tutor

---

## AI Character Profile

The app offers multiple Parisian characters to choose from. Users select their preferred pen-pal during onboarding and can switch later.

### 1. Lucas - The Hipster

**Identity**:
- **Age**: 26 years old
- **Location**: Belleville, Paris (2026)
- **Background**: Works in a creative field (graphic design/marketing), hangs out at Canal Saint-Martin
- **Interests**: Watches shows like *Lupin*, listens to PNL, Angèle, indie music scene, street art, vintage clothes

**Personality & Communication Style**:
- Casual, friendly, slightly ironic
- Uses "tu" not "vous" (informal)
- Warm but not overly enthusiastic—authentically Parisian
- Slightly cynical but good-natured

### 2. Sarah - The Professional

**Identity**:
- **Age**: 28 years old
- **Location**: Montmartre, Paris (2026)
- **Background**: Marketing manager at a startup, career-focused but sociable
- **Interests**: Business podcasts, fitness, wine tasting, networking events, travel

**Personality & Communication Style**:
- Ambitious, articulate, well-organized
- Still uses "tu" (peer-to-peer) but slightly more polished
- Shares stories about work and ambitions
- Likes planning and organizing activities

### 3. Karim - The Student

**Identity**:
- **Age**: 24 years old
- **Location**: 5th arrondissement (Latin Quarter), Paris (2026)
- **Background**: Master's student in sociology, works part-time at a café
- **Interests**: Football, video games, student life, music festivals, politics

**Personality & Communication Style**:
- Enthusiastic, energetic, curious
- Very casual, lots of current slang
- Asks many questions about user's life
- Talks about studies, future plans, social issues

### Shared Language Characteristics

**All characters**:
- Start at **B2+ level**, gradually increase complexity based on user comprehension
- **No spelling or grammatical mistakes** (all are well-educated)
- Uses contemporary Parisian slang appropriate to their personality (verlan, "wesh", "c'est ouf", "avoir la flemme", "ça craint")
- Explains vocabulary through context, not explicit teaching
- References current Parisian life, culture, daily experiences

---

## User Profile

- **Current French Level**: B1/B2 (intermediate to upper-intermediate)
- **Learning Goal**: Progress from B2 to C1/C2 gradually through immersion and practice
- **Use Case**: Casual, regular practice through natural conversation
- **Progression**: AI adapts complexity based on demonstrated comprehension over time

---

## Core Features

### 1. Chat Interface

**Layout**:
- WhatsApp/iMessage style interface
- User messages on right, AI messages on left
- Clean, modern messaging UI
- Text-only (no voice)

### 2. Pre-Send Correction System

**Correction Flow**:
- User types message freely without interruption
- When ready to review, user triggers error check via gesture:
  - Long-press on the composed message, or
  - Tap a "Vérifier" (Check) button next to send button
- Grammar and spelling errors highlighted in the message input
- Different colors for different error types (e.g., red = spelling, yellow = grammar, blue = word choice)
- Click/tap on highlighted text to see detailed explanation

**Feedback Panel**:
- Appears above the message input area (collapsible)
- Shows comprehensive breakdown of all errors in current message
- For each error: explanation + correct form + why it's wrong
- Suggestions for improvement
- Can be dismissed to view full chat history

**User Choice**:
- User can edit message based on feedback
- **"Envoyer quand même" (Send Anyway) button** allows ignoring corrections
- No mandatory correction—user controls learning pace

### 3. Conversation Structure

**Natural Scaffolding**:
- Conversations have shared context and continuity
- AI remembers details from previous chats (user's preferences, stories shared, ongoing topics)
- Topics emerge naturally rather than forced lessons
- Examples of ongoing contexts:
  - Planning a party or gathering
  - Discussing a movie both watched
  - Preparing for a trip
  - Sharing daily life updates

**Conversation Flow**:
- AI initiates with context-rich opening when appropriate
- Maintains thread of conversation across sessions
- References previous discussions to create continuity
- Asks follow-up questions to maintain engagement

### 4. Conversation Flow

1. User types message in French
2. When finished typing/thinking, user triggers error check via gesture (long-press message or tap "Vérifier")
3. Error detection runs and highlighting appears
4. User reviews inline cues and feedback panel
5. User can edit or proceed
6. User sends message (with or without corrections)
7. AI responds in French at appropriate level with natural Parisian personality
8. User can long-press AI messages for translation or word explanations
9. AI maintains conversation thread and references previous interactions
10. Cycle repeats

**Translation Feature**:
- Long-press any AI message to reveal translation below the message
- Translation appears inline, can be dismissed
- Also shows for individual words/phrases when long-pressed

---

## Learning Features

### 1. Error Tracking

**Personal Dashboard**:
- Tracks most common error types (verb conjugations, gender of nouns, prepositions, etc.)
- Shows error frequency over time
- Identifies patterns in mistakes

**Learning Insights**:
- Displays progress on specific grammar concepts
- Highlights improvement areas
- Shows error reduction trends over time

### 2. Vocabulary Builder

**Word Saving**:
- Long-press any word in AI's message to save it
- Automatically saves words you misspelled or used incorrectly
- Each saved word includes: definition, example sentence, your mistake (if applicable)

**Natural Reinforcement**:
- AI naturally incorporates previously saved words selected for natural reinforcement into future conversations when contextually appropriate
- No forced usage or artificial challenges—appears organically in conversation
- User encounters saved words in new contexts, reinforcing learning naturally
- Example: If user saved "balade" (walk/stroll), AI might later say "Ça te dit une balade au canal ce week-end ?"

**Saved Words Library**:
- Dedicated section to browse all saved words and phrases
- Each entry displays:
  - The French word or phrase
  - English translation
  - Example sentence in context
  - Source conversation (link back to where it was encountered)
- Search and filter functionality (alphabetical, by date saved, by conversation)
- Organized into categories: Words from AI messages, Words I struggled with, Manually saved
- **Natural Reinforcement Selection**:
  - Toggle switch on each word to include/exclude from natural reinforcement
  - All saved words default to "selected" (included in word bank)
  - Users can deselect words they don't want the AI to use
  - Visual indicator showing which words are active in the reinforcement pool
  - Bulk select/deselect options (e.g., "Deselect all", "Select all")

### 3. Conversation History

- Full searchable chat history
- Review past mistakes and corrections
- See your improvement over time

---

## UX/UI Specifications

### Visual Design

**Color Scheme**:
- Clean, modern aesthetic
- Distinct colors for different error types
- Paris-inspired accents (subtle blues, creams, maybe a touch of red)

**Typography**:
- Clear, readable fonts
- Good distinction between user's message and AI's message

**Key Screens**:

1. **Chat Screen** (main):
   - Message list (scrollable)
   - Message input with inline highlighting
   - Feedback panel (expandable/collapsible)
   - Send and "Send Anyway" buttons

2. **Word Detail Modal**:
   - Triggered by long-pressing AI message
   - Shows translation, definition, context
   - Save word option

3. **Dashboard/Profile**:
   - Error statistics
   - Vocabulary list
   - Progress charts
   - Settings

### Interactions

- **Inline Error Tapping**: Opens detailed explanation popup
- **Long-press AI Message**: Shows translation options and word definitions
- **Swipe Actions**: Quick save words, view message details
- **Pull to Refresh**: Refresh conversation or load more history

---

## Technical Considerations

### LLM Integration

**Requirements**:
- Real-time grammar/spelling correction API
- Conversational AI for the Parisian character
- Possibly separate endpoints for different functions:
  - `/correct` - Pre-send error detection
  - `/chat` - AI conversation responses
  - `/translate` - On-demand translations
  - `/explain` - Word/phrase explanations

**Character Consistency**:
- Strong system prompt defining Lucas/Théo's personality
- Temperature settings for natural but consistent responses
- Memory of conversation context (maintain personality across messages)

### Data Storage

**Local Storage**:
- All data stored locally on device
- Includes: Chat history, Saved vocabulary, User settings, Error tracking data, Natural reinforcement word bank selections

**File-Based Transfer (No Cloud Sync)**:
- **"Sauvegarder l'état" (Save App State)**:
  - Option in Settings to export all app data
  - Generates a compressed state file (.mpp backup format)
  - User chooses save location (Downloads, Google Drive local folder, etc.)
  - File contains: complete conversation history, vocabulary library, settings, error tracking, character preferences
  
- **"Restaurer depuis fichier" (Sync from File)**:
  - Option in Settings to import state from backup file
  - User selects .mpp file from device storage
  - App resets to the saved state (replaces current data)
  - Confirmation dialog warns that current data will be overwritten
  - Option to create backup of current state before restoring

**Migration Flow**:
1. User finishes session on Device A
2. User goes to Settings → "Sauvegarder l'état" → Saves .mpp file to cloud storage folder (Drive, Dropbox, etc.) or transfers via cable/email
3. User opens app on Device B
4. User goes to Settings → "Restaurer depuis fichier" → Selects the .mpp file
5. App loads complete state from Device A

**No Cloud Sync**: For now, no automatic cloud synchronization. Manual file-based transfer only.

### Error Detection

**Approach**:
- LLM-based grammar checking (more nuanced than rule-based)
- French-specific error patterns
- Context-aware corrections

**Alternative**:
- Hybrid approach: rule-based for common errors + LLM for complex analysis

### Performance

- Real-time highlighting needs to be responsive (< 200ms)
- Consider debouncing as user types
- Offline mode: basic functionality without API (just chat history viewing)

---

## Edge Cases & Considerations

### When User Sends Incorrect French
- AI should understand anyway (natural conversation)
- May gently correct through modeling ("Oh, tu veux dire... [correct form]")
- Shouldn't break character to "teach" explicitly

### Cultural References
- AI should explain references if asked
- Keep references current and authentic to 2026 Paris
- Balance between educational and immersive

### Profanity/Slang Boundaries
- Use authentic Parisian slang but keep it appropriate
- No offensive language, but "colorful" expressions acceptable
- User settings to adjust slang level if desired

### Frustration Handling
- If user struggles, AI should adapt slightly (simpler sentences) while maintaining appropriate challenge level
- Never switches to English
- Encouraging but not patronizing

### Cultural Authenticity & Updates
- Characters evolve with the times (monthly updates with current references)
- AI can mention recent events ("Did you see the match yesterday?")
- Seasonal adjustments (Christmas markets, summer festivals, current exhibitions)
- References to shows, music, and trends stay current
- Cultural context feels alive and dynamic, not static

---

## Future Enhancements (Post-MVP)

1. **Voice Messages**: Send voice, get transcription + feedback, AI responds with voice
2. **Photo Sharing**: Share photos, AI describes in French, conversation about image
3. **Group Chats**: Multiple AI characters in one conversation
4. **Cultural Deep Dives**: Special conversations about specific topics (French cinema, politics, food)
5. **Certification Prep**: Mode focused on DELF/DALF exam preparation

---

## Success Metrics

- User retention (daily/weekly active users)
- Conversation length and frequency
- Error reduction over time
- Vocabulary acquisition rate
- User-reported confidence improvement
- Natural conversation flow (not too many corrections per message after initial learning curve)

---

## Brand & Tone

**App Name**: Mon Pote Parisien
**Tagline Ideas**:
- "Ton pote parisien pour progresser en français"
- "Apprends le français qu'on parle vraiment à Paris"
- "Pas de prof, juste un pote"

**Tone of Voice**:
- Friendly, approachable
- Authentic, not corporate
- Empowering but relaxed
- For language learners who are serious but don't want to feel like they're in school

---

## Summary

This app fills the gap between formal language learning apps (Duolingo, Babbel) and actual conversation practice. It's for the learner who:
- Has intermediate French (B1/B2)
- Wants to progress gradually to C1/C2 through immersion
- Wants to sound natural and current
- Prefers immersion over explicit instruction
- Wants correction but not in the middle of conversation
- Wants to understand real Parisians, not textbook French
- Values natural conversation flow over gamification

**Key Differentiators**:
- **Pre-send correction flow** separates learning from conversation
- **Multiple authentic characters** to choose from (Lucas, Sarah, Karim)
- **Gradual level progression** from B2+ to C1/C2 based on comprehension
- **Natural vocabulary reinforcement** without forced challenges
- **Living cultural context** that stays current with real Parisian life
