# UI Redesign — Design Spec

## Overview

Complete visual overhaul of Mon Pote Parisien from dark theme to a warm minimal light theme with Parisian Blue accent, spring animations on all interactions, and Material 3 Expressive design principles.

**Goal:** Transform the app from a basic dark chat UI into a polished, modern, animation-rich experience that feels premium and alive.

## Scope

This spec covers:
- New color palette (warm minimal + Parisian Blue)
- Redesigned chat screen (light bubbles, timestamps outside, squircle avatars)
- Redesigned onboarding screen (white cards, staggered entrance)
- Redesigned correction panel (bottom sheet style)
- Redesigned translation drawer (narrower, tucked under bubble, hold-to-reveal with bounce)
- Spring animations on all interactions
- Updated Compose theme

This spec does **not** cover: new features, data model changes, or API changes. This is purely visual.

## Design System

### Color Palette

| Token | Value | Usage |
|-------|-------|-------|
| Background | #FAF8F5 | Screen backgrounds (warm off-white) |
| Surface | #FFFFFF | Cards, bubbles, input bar |
| SurfaceVariant | #F5F3F0 | Input field background, secondary surfaces |
| SurfaceTint | #F0EDE8 | Translation drawer, date dividers |
| Primary | #2563EB | User bubbles, send button, links, active states |
| PrimaryContainer | #DBEAFE | Primary tinted backgrounds |
| OnBackground | #1A1A1A | Primary text |
| OnSurface | #333333 | Body text in cards/bubbles |
| TextSecondary | #888888 | Explanations, descriptions |
| TextMuted | #AAAAAA | Timestamps, hints, placeholders |
| TextFaint | #CCCCCC | Dividers, dismiss buttons |
| ErrorRed | #EF4444 | Orthographe errors, error states |
| GrammarOrange | #F97316 | Grammaire errors |
| StyleBlue | #2563EB | Style suggestions (same as Primary) |
| SuccessGreen | #059669 | Corrections, Vérifier button |
| LucasGradient | #7B68EE → #A78BFA | Lucas avatar/tag |
| SarahGradient | #F97316 → #FB923C | Sarah avatar/tag |
| KarimGradient | #059669 → #34D399 | Karim avatar/tag |
| InputText | #333333 | Text typed in input field (same as OnSurface) |
| DateDividerText | #999999 | Date divider pill text |

### Character Gradients

Gradients are hardcoded in the UI layer by character ID (not in the data model — no schema changes). The `CharacterAvatar` composable maps `character.id` to a `Brush.linearGradient`:
- `"lucas"` → `listOf(Color(0xFF7B68EE), Color(0xFFA78BFA))`
- `"sarah"` → `listOf(Color(0xFFF97316), Color(0xFFFB923C))`
- `"karim"` → `listOf(Color(0xFF059669), Color(0xFF34D399))`

Fallback: if ID doesn't match, use `character.color` as a flat fill (backward compatible).

### Typography

Same font sizes as current, but text colors change from light-on-dark to dark-on-light:
- Headlines/titles: OnBackground (#1A1A1A)
- Body text: OnSurface (#333333)
- Secondary: TextSecondary (#888888)
- Timestamps: TextMuted (#AAAAAA)

### Shape

- Message bubbles: 18dp corners (AI: 4dp top-left, User: 4dp top-right)
- Cards: 20dp corners
- Avatars: 16dp corners (squircle, not circle) — all sizes
- Input field: 22dp corners (pill shape)
- Buttons: 50% corners (circle)
- Tag pills: 10dp corners
- Translation drawer: 0dp top, 12dp bottom (tucked under bubble)
- Correction panel: 20dp top corners, 0dp bottom (bottom sheet)

### Shadows

- Card shadow: `0dp 2dp 8dp rgba(0,0,0,0.04)` — subtle depth
- Send button shadow: `0dp 2dp 8dp` with 30% opacity of button color — colored glow
- Correction panel shadow: `0dp -4dp 20dp rgba(0,0,0,0.06)` — upward shadow
- No shadows on message bubbles from user (flat)
- Light shadow on AI bubbles: `0dp 1dp 3dp rgba(0,0,0,0.04)`

## Animations

Layout animations (appear, expand, collapse) use spring physics. Gesture feedback animations (bounce, hold indicator) use short fixed durations since they need precise timing tied to user input.

### Message Appear

New messages slide up from the bottom and fade in simultaneously.
- Spring: `dampingRatio = 0.7, stiffness = 300`
- Offset: start 40dp below, animate to 0
- Alpha: 0 → 1 over the first 60% of the spring

### Translation Drawer

Three-phase interaction:
1. **Hold phase**: user presses and holds AI bubble. A small circular progress indicator (20dp, blue stroke) appears in the top-right corner of the bubble and fills over 400ms.
2. **Trigger phase**: at 400ms, the hold registers. The bubble bounces once (scale 1.0 → 0.96 → 1.02 → 1.0 over 300ms, ease-out curve, single bounce). Haptic feedback fires. The translation drawer begins sliding out.
3. **Drawer animation**: `expandVertically()` with spring `dampingRatio = 0.8, stiffness = 400`. The drawer is narrower than the bubble (inset 8dp on each side) with 0dp top corners (tucked under bubble) and 12dp bottom corners.

Repeat long-press to retract (same bounce, `shrinkVertically`).

### Card Press (Onboarding)

On tap of character card:
- Scale: 1.0 → 0.97 → 1.0 (single bounce, 250ms)
- Shadow increases slightly during press
- Haptic feedback on release

### Card Stagger Entrance (Onboarding)

Cards slide up from bottom with staggered delays:
- Card 1: 0ms delay
- Card 2: 80ms delay
- Card 3: 160ms delay
- Spring: `dampingRatio = 0.7, stiffness = 300`
- Offset: start 60dp below
- Alpha: 0 → 1

### Send Button Press

Scale: 1.0 → 0.9 → 1.0 (quick bounce, 200ms). Colored shadow pulses slightly.

### Vérifier Button Press

Same as send button. Haptic feedback on press.

### Correction Panel

Slides up from bottom using `AnimatedVisibility` + `expandVertically`. Spring: `dampingRatio = 0.8, stiffness = 400`. Error cards inside stagger in (same pattern as onboarding cards).

### Typing Indicator

Existing bouncing dots animation remains but uses spring physics instead of tween. Dots bounce with slight overshoot.

## Screen Specifications

### Chat Screen

**Top bar:**
- White (#FFF) background with subtle bottom divider (1px #F0F0F0)
- Back arrow (Parisian Blue)
- Squircle avatar (38dp, 16dp corners) with character gradient
- Character name (14sp, 600 weight, #1A1A1A)
- Location subtitle (11sp, #AAAAAA)
- Overflow menu icon (#CCCCCC)

**Message list:**
- Background: #FAF8F5
- Date dividers: centered pill, #F0EDE8 background, #999 text, 10dp corners
- AI bubbles: #FFF background, shadow, 4dp/18dp corners, text in #333
- User bubbles: #2563EB background, no shadow, 18dp/4dp corners, text in #FFF
- Timestamps: outside bubbles, below. Left-aligned under AI bubbles, right-aligned under user bubbles. 10sp, TextMuted (#AAAAAA). Horizontal padding matches bubble edge (4dp indent).
- No avatars in message list (cleaner — avatar is in top bar). This includes the typing indicator — remove its inline avatar too.

**Translation drawer:**
- #F0EDE8 background (warm beige)
- Narrower than bubble: inset 8dp each side
- 0dp top corners, 12dp bottom corners
- "EN" label: Parisian Blue, 9sp, bold
- Translation text: #888, 11.5sp, italic
- Margin-top: -6dp (tucks under bubble)

**Input area:**
- White (#FFF) background with subtle top divider
- Input field: #F5F3F0 background, 22dp pill shape, 13sp
- Vérifier button: #059669 (green), circle, appears when text is non-empty
- Send button: #2563EB with colored drop shadow
- Both buttons: 34dp size

### Onboarding Screen

**Header:**
- Background: #FAF8F5
- French flag emoji: 32sp
- Title: "Mon Pote Parisien", 24sp, 700 weight, #1A1A1A
- Subtitle: "Choisis ton pote", 13sp, #AAAAAA

**Character cards:**
- White (#FFF) with 20dp corners and soft shadow
- Squircle avatar: 52dp, 16dp corners, gradient background
- Name: 16sp, 600 weight, #1A1A1A
- Tag pill: tinted background matching character color, 10sp
- Location: 11sp, #BBBBBB
- Description: 12sp, #888888

**Footer:**
- "Tu pourras changer de pote plus tard", 11sp, #CCCCCC

### Correction Panel

**Panel container:**
- White (#FFF) background
- 20dp top corners (bottom sheet style)
- Upward box shadow
- Drag handle: 36dp × 4dp, #DDD, centered

**Header:**
- Error icon: 24dp square with #FEF2F2 background, 8dp corners, red "!" inside
- Count: "3 corrections", 14sp, 600 weight, #1A1A1A
- Dismiss: "✕", #CCC

**Error cards:**
- #FAF8F5 background, 14dp corners
- Left color bar: 3dp wide, 2dp rounded, error-type color
- Error type label: 11sp, 600 weight, matching color
- Original text: #AAA, strikethrough
- Arrow: " → ", #CCC
- Correction: #059669, 500 weight
- Explanation: #AAA, 11sp

**State banners** (loading, success, error):
- Same position as panel, same rounded top corners
- Loading: #F5F3F0 background, grey spinner + text
- Success: #059669 at 8% opacity background, green text
- Error: #EF4444 at 8% opacity background, red text

## Files to Modify

All changes are in existing files — no new modules.

**`:core:ui`**
- `theme/Color.kt` — replace entire color palette
- `theme/Theme.kt` — switch to light color scheme
- `theme/Type.kt` — update text colors for light theme
- `components/CharacterAvatar.kt` — change from circle to squircle (RoundedCornerShape 16dp)

**`:feature:chat`**
- `ChatUiState.kt` — no changes needed. The hold indicator progress is local composable state within `MessageBubble` (not ViewModel state), since it's purely visual feedback for a single gesture.
- `components/MessageBubble.kt` — light theme colors, timestamps outside, remove inline avatar, add long-press hold indicator + bounce animation
- `components/TranslationCard.kt` — narrower width, warm beige color, tucked under bubble
- `components/ChatInput.kt` — light theme colors, updated button colors/shadows
- `components/ChatTopBar.kt` — white background, squircle avatar, updated colors
- `components/TypingIndicator.kt` — light theme colors, spring physics dots
- `components/FeedbackPanel.kt` — bottom sheet style, light cards, drag handle, updated colors
- `ChatScreen.kt` — light background, date dividers, pass hold indicator state, message appear animation

**`:feature:onboarding`**
- `OnboardingScreen.kt` — light theme, white cards, staggered entrance animation, card press animation
