# Mon Pote Parisien MVP Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a native Android chat app where users pick a Parisian character and have French conversations powered by Azure OpenAI.

**Architecture:** Multi-module Kotlin/Compose app with 7 Gradle modules (app, 2 feature, 4 core). MVVM pattern with Hilt DI. Room for local persistence, Retrofit for Azure OpenAI API calls, Compose Navigation for routing.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), Hilt, Room, Retrofit + OkHttp, Moshi, Coroutines + Flow, Preferences DataStore, Azure OpenAI

---

## File Structure

```
MonPoteParisien/
├── settings.gradle.kts                          # Module declarations
├── build.gradle.kts                             # Root build config (plugins, versions)
├── gradle.properties                            # Gradle settings
├── local.properties                             # Azure OpenAI secrets (gitignored)
│
├── app/
│   ├── build.gradle.kts                         # App module deps (features + Hilt)
│   └── src/main/
│       ├── AndroidManifest.xml                  # App manifest (INTERNET permission)
│       └── java/com/monpote/
│           ├── MonPoteApplication.kt            # @HiltAndroidApp entry
│           ├── MainActivity.kt                  # Single activity, Compose host
│           └── navigation/
│               └── NavGraph.kt                  # Navigation routes + launch logic
│
├── core/
│   ├── model/
│   │   ├── build.gradle.kts                     # Pure Kotlin module (no Android)
│   │   └── src/main/java/com/monpote/core/model/
│   │       ├── Character.kt                     # Character domain model
│   │       ├── Conversation.kt                  # Conversation domain model
│   │       └── Message.kt                       # Message domain model + Role enum
│   │
│   ├── database/
│   │   ├── build.gradle.kts                     # Room + Hilt deps
│   │   └── src/main/java/com/monpote/core/database/
│   │       ├── MonPoteDatabase.kt               # Room DB definition + seed callback
│   │       ├── entity/
│   │       │   ├── CharacterEntity.kt           # Room entity for Character
│   │       │   ├── ConversationEntity.kt        # Room entity for Conversation
│   │       │   └── MessageEntity.kt             # Room entity for Message
│   │       ├── dao/
│   │       │   ├── CharacterDao.kt              # Character queries
│   │       │   ├── ConversationDao.kt           # Conversation CRUD
│   │       │   └── MessageDao.kt                # Message CRUD + Flow queries
│   │       ├── mapper/
│   │       │   └── EntityMappers.kt             # Entity ↔ domain model mappers
│   │       ├── seed/
│   │       │   └── CharacterSeeder.kt           # Predefined character data + system prompts
│   │       └── di/
│   │           └── DatabaseModule.kt            # Hilt module providing DB + DAOs
│   │
│   ├── network/
│   │   ├── build.gradle.kts                     # Retrofit + OkHttp + Moshi deps
│   │   └── src/main/java/com/monpote/core/network/
│   │       ├── api/
│   │       │   └── AzureOpenAiService.kt        # Retrofit interface
│   │       ├── dto/
│   │       │   ├── ChatRequest.kt               # Request DTO (messages, temp, max_tokens)
│   │       │   └── ChatResponse.kt              # Response DTO (choices, message content)
│   │       ├── interceptor/
│   │       │   └── ApiKeyInterceptor.kt         # OkHttp interceptor for api-key header
│   │       └── di/
│   │           └── NetworkModule.kt             # Hilt module providing Retrofit + OkHttp
│   │
│   └── ui/
│       ├── build.gradle.kts                     # Compose deps
│       └── src/main/java/com/monpote/core/ui/
│           ├── theme/
│           │   ├── Color.kt                     # App color palette
│           │   ├── Type.kt                      # Typography definitions
│           │   └── Theme.kt                     # MonPoteTheme composable
│           └── components/
│               └── CharacterAvatar.kt           # Reusable avatar composable
│
├── feature/
│   ├── onboarding/
│   │   ├── build.gradle.kts                     # core:database, core:model, core:ui deps
│   │   └── src/main/java/com/monpote/feature/onboarding/
│   │       ├── OnboardingScreen.kt              # Character selection UI
│   │       └── OnboardingViewModel.kt           # Selection logic + DataStore write
│   │
│   └── chat/
│       ├── build.gradle.kts                     # All core module deps
│       └── src/main/java/com/monpote/feature/chat/
│           ├── ChatScreen.kt                    # Chat screen composable (orchestrates components)
│           ├── ChatViewModel.kt                 # Send/receive logic, state management
│           ├── ChatUiState.kt                   # UI state data class
│           └── components/
│               ├── ChatTopBar.kt                # Top bar with character info + overflow menu
│               ├── MessageBubble.kt             # Single message bubble
│               ├── ChatInput.kt                 # Text field + send button
│               └── TypingIndicator.kt           # Animated dots
```

---

## Chunk 1: Project Scaffolding + Core Model

### Task 1: Create Android Project and Module Structure

This task sets up the multi-module Gradle project from scratch in Android Studio.

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (root)
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `core/model/build.gradle.kts`
- Create: `core/database/build.gradle.kts`
- Create: `core/network/build.gradle.kts`
- Create: `core/ui/build.gradle.kts`
- Create: `feature/onboarding/build.gradle.kts`
- Create: `feature/chat/build.gradle.kts`

- [ ] **Step 1: Create new Android project in Android Studio**

Open Android Studio → New Project → Empty Activity. Settings:
- Name: `Mon Pote Parisien`
- Package: `com.monpote`
- Language: Kotlin
- Min SDK: API 26 (Android 8.0)
- Build configuration language: Kotlin DSL

This creates the `app/` module and root build files.

- [ ] **Step 2: Add version catalog**

Edit `gradle/libs.versions.toml` to define all dependency versions in one place:

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
ksp = "2.1.0-1.0.29"
hilt = "2.54"
room = "2.6.1"
retrofit = "2.11.0"
okhttp = "4.12.0"
moshi = "1.15.1"
coroutines = "1.9.0"
lifecycle = "2.8.7"
navigation = "2.8.5"
composeBom = "2024.12.01"
datastore = "1.1.1"
hiltNavigationCompose = "1.2.0"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Lifecycle
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Network
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
moshi = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshi" }
moshi-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshi" }

# Coroutines
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Core Android
core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.15.0" }
activity-compose = { group = "androidx.activity", name = "activity-compose", version = "1.9.3" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 3: Configure root build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

- [ ] **Step 4: Create core:model module**

Create directory `core/model/src/main/java/com/monpote/core/model/`.

`core/model/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}
```

This is a pure Kotlin module — no Android dependencies.

- [ ] **Step 5: Create core:database module**

Create directory `core/database/src/main/java/com/monpote/core/database/`.

`core/database/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.monpote.core.database"
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
    implementation(project(":core:model"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.coroutines.android)
}
```

- [ ] **Step 6: Create core:network module**

Create directory `core/network/src/main/java/com/monpote/core/network/`.

`core/network/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.monpote.core.network"
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
    implementation(project(":core:model"))

    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.coroutines.android)
}
```

- [ ] **Step 7: Create core:ui module**

Create directory `core/ui/src/main/java/com/monpote/core/ui/`.

`core/ui/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.monpote.core.ui"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:model"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}
```

- [ ] **Step 8: Create feature:onboarding module**

Create directory `feature/onboarding/src/main/java/com/monpote/feature/onboarding/`.

`feature/onboarding/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.monpote.feature.onboarding"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
}
```

- [ ] **Step 9: Create feature:chat module**

Create directory `feature/chat/src/main/java/com/monpote/feature/chat/`.

`feature/chat/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.monpote.feature.chat"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coroutines.android)
}
```

- [ ] **Step 10: Update settings.gradle.kts to include all modules**

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MonPoteParisien"

include(":app")
include(":core:model")
include(":core:database")
include(":core:network")
include(":core:ui")
include(":feature:onboarding")
include(":feature:chat")
```

- [ ] **Step 11: Update app/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.monpote"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.monpote"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:chat"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.core.ktx)
}
```

- [ ] **Step 12: Sync Gradle and verify build**

Run: Android Studio → File → Sync Project with Gradle Files

Then build:
```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL (may have warnings, but no errors). All 7 modules resolve.

- [ ] **Step 13: Commit**

```bash
git add -A
git commit -m "scaffold: multi-module project structure with 7 Gradle modules"
```

### Task 2: Core Domain Models

**Files:**
- Create: `core/model/src/main/java/com/monpote/core/model/Character.kt`
- Create: `core/model/src/main/java/com/monpote/core/model/Conversation.kt`
- Create: `core/model/src/main/java/com/monpote/core/model/Message.kt`

**Note:** The spec's data model table lists `avatarRes: Int` on Character. The plan replaces this with `tag`, `location`, and `color` fields — these are derived from the spec's onboarding screen description (which calls for character initials with signature colors, tags like "Le Hipster", and neighborhoods). The avatar is generated from initial + color, so `avatarRes` is not needed.

- [ ] **Step 1: Create Character model**

`core/model/src/main/java/com/monpote/core/model/Character.kt`:
```kotlin
package com.monpote.core.model

data class Character(
    val id: String,
    val name: String,
    val tag: String,
    val description: String,
    val location: String,
    val systemPrompt: String,
    val color: Long,
)
```

- [ ] **Step 2: Create Conversation model**

`core/model/src/main/java/com/monpote/core/model/Conversation.kt`:
```kotlin
package com.monpote.core.model

data class Conversation(
    val id: Long = 0,
    val characterId: String,
    val title: String? = null,
    val createdAt: Long,
    val lastMessageAt: Long,
)
```

- [ ] **Step 3: Create Message model with Role enum**

`core/model/src/main/java/com/monpote/core/model/Message.kt`:
```kotlin
package com.monpote.core.model

enum class Role(val value: String) {
    USER("user"),
    ASSISTANT("assistant"),
}

data class Message(
    val id: Long = 0,
    val conversationId: Long,
    val content: String,
    val role: Role,
    val timestamp: Long,
)
```

- [ ] **Step 4: Verify build**

```bash
./gradlew :core:model:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add core/model/
git commit -m "feat: add core domain models (Character, Conversation, Message)"
```

---

## Chunk 2: Database Layer

### Task 3: Room Entities

**Files:**
- Create: `core/database/src/main/java/com/monpote/core/database/entity/CharacterEntity.kt`
- Create: `core/database/src/main/java/com/monpote/core/database/entity/ConversationEntity.kt`
- Create: `core/database/src/main/java/com/monpote/core/database/entity/MessageEntity.kt`

- [ ] **Step 1: Create CharacterEntity**

`core/database/src/main/java/com/monpote/core/database/entity/CharacterEntity.kt`:
```kotlin
package com.monpote.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tag: String,
    val description: String,
    val location: String,
    val systemPrompt: String,
    val color: Long,
)
```

- [ ] **Step 2: Create ConversationEntity**

`core/database/src/main/java/com/monpote/core/database/entity/ConversationEntity.kt`:
```kotlin
package com.monpote.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index("characterId")],
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterId: String,
    val title: String? = null,
    val createdAt: Long,
    val lastMessageAt: Long,
)
```

- [ ] **Step 3: Create MessageEntity**

`core/database/src/main/java/com/monpote/core/database/entity/MessageEntity.kt`:
```kotlin
package com.monpote.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("conversationId")],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val content: String,
    val role: String,
    val timestamp: Long,
)
```

- [ ] **Step 4: Verify build**

```bash
./gradlew :core:database:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add core/database/src/main/java/com/monpote/core/database/entity/
git commit -m "feat: add Room entities (Character, Conversation, Message)"
```

### Task 4: Entity Mappers

**Files:**
- Create: `core/database/src/main/java/com/monpote/core/database/mapper/EntityMappers.kt`

- [ ] **Step 1: Create mappers between entities and domain models**

`core/database/src/main/java/com/monpote/core/database/mapper/EntityMappers.kt`:
```kotlin
package com.monpote.core.database.mapper

import com.monpote.core.database.entity.CharacterEntity
import com.monpote.core.database.entity.ConversationEntity
import com.monpote.core.database.entity.MessageEntity
import com.monpote.core.model.Character
import com.monpote.core.model.Conversation
import com.monpote.core.model.Message
import com.monpote.core.model.Role

fun CharacterEntity.toDomain() = Character(
    id = id,
    name = name,
    tag = tag,
    description = description,
    location = location,
    systemPrompt = systemPrompt,
    color = color,
)

fun Character.toEntity() = CharacterEntity(
    id = id,
    name = name,
    tag = tag,
    description = description,
    location = location,
    systemPrompt = systemPrompt,
    color = color,
)

fun ConversationEntity.toDomain() = Conversation(
    id = id,
    characterId = characterId,
    title = title,
    createdAt = createdAt,
    lastMessageAt = lastMessageAt,
)

fun Conversation.toEntity() = ConversationEntity(
    id = id,
    characterId = characterId,
    title = title,
    createdAt = createdAt,
    lastMessageAt = lastMessageAt,
)

fun MessageEntity.toDomain() = Message(
    id = id,
    conversationId = conversationId,
    content = content,
    role = Role.entries.first { it.value == role },
    timestamp = timestamp,
)

fun Message.toEntity() = MessageEntity(
    id = id,
    conversationId = conversationId,
    content = content,
    role = role.value,
    timestamp = timestamp,
)
```

- [ ] **Step 2: Verify build**

```bash
./gradlew :core:database:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core/database/src/main/java/com/monpote/core/database/mapper/
git commit -m "feat: add entity-to-domain mappers"
```

### Task 5: DAOs

**Files:**
- Create: `core/database/src/main/java/com/monpote/core/database/dao/CharacterDao.kt`
- Create: `core/database/src/main/java/com/monpote/core/database/dao/ConversationDao.kt`
- Create: `core/database/src/main/java/com/monpote/core/database/dao/MessageDao.kt`

- [ ] **Step 1: Create CharacterDao**

`core/database/src/main/java/com/monpote/core/database/dao/CharacterDao.kt`:
```kotlin
package com.monpote.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.monpote.core.database.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: String): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)
}
```

- [ ] **Step 2: Create ConversationDao**

`core/database/src/main/java/com/monpote/core/database/dao/ConversationDao.kt`:
```kotlin
package com.monpote.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.monpote.core.database.entity.ConversationEntity

@Dao
interface ConversationDao {
    @Insert
    suspend fun insert(conversation: ConversationEntity): Long

    @Query("SELECT * FROM conversations WHERE characterId = :characterId ORDER BY lastMessageAt DESC LIMIT 1")
    suspend fun getLatestConversation(characterId: String): ConversationEntity?

    @Query("UPDATE conversations SET lastMessageAt = :timestamp WHERE id = :conversationId")
    suspend fun updateLastMessageAt(conversationId: Long, timestamp: Long)
}
```

- [ ] **Step 3: Create MessageDao**

`core/database/src/main/java/com/monpote/core/database/dao/MessageDao.kt`:
```kotlin
package com.monpote.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.monpote.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(conversationId: Long, limit: Int = 20): List<MessageEntity>

    @Insert
    suspend fun insert(message: MessageEntity): Long
}
```

- [ ] **Step 4: Verify build**

```bash
./gradlew :core:database:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add core/database/src/main/java/com/monpote/core/database/dao/
git commit -m "feat: add Room DAOs (Character, Conversation, Message)"
```

### Task 6: Character Seeder and Database Definition

**Files:**
- Create: `core/database/src/main/java/com/monpote/core/database/seed/CharacterSeeder.kt`
- Create: `core/database/src/main/java/com/monpote/core/database/MonPoteDatabase.kt`
- Create: `core/database/src/main/java/com/monpote/core/database/di/DatabaseModule.kt`

- [ ] **Step 1: Create CharacterSeeder with system prompts**

`core/database/src/main/java/com/monpote/core/database/seed/CharacterSeeder.kt`:
```kotlin
package com.monpote.core.database.seed

import com.monpote.core.database.entity.CharacterEntity

object CharacterSeeder {
    fun getCharacters(): List<CharacterEntity> = listOf(
        CharacterEntity(
            id = "lucas",
            name = "Lucas",
            tag = "Le Hipster",
            description = "Graphiste, fan de PNL et de street art. Traîne au bord du canal avec un café. Décontracté, un peu ironique, toujours sympa.",
            location = "Belleville, Paris",
            systemPrompt = """
Tu es Lucas, 26 ans, graphiste freelance qui habite à Belleville, Paris. Tu traînes souvent du côté du Canal Saint-Martin.

Ta personnalité :
- Décontracté, amical, légèrement ironique mais bienveillant
- Authentiquement parisien — pas trop enthousiaste, cool mais sympa
- Tu utilises "tu" (jamais "vous")
- Tu adores le street art, les friperies, la musique (PNL, Angèle, la scène indie)
- Tu regardes des séries comme Lupin
- Tu fréquentes les marchés de créateurs, les expos, les bars à café artisanaux

Règles de langage :
- Tu parles en français à un niveau B2+ et tu augmentes progressivement la complexité selon la compréhension de ton interlocuteur
- Tu NE fais JAMAIS de fautes d'orthographe ou de grammaire (tu es éduqué)
- Tu utilises du verlan et de l'argot parisien actuel naturellement : "c'est ouf", "avoir la flemme", "ça craint", "wesh", "bg", "le taf"
- Tu NE passes JAMAIS à l'anglais, même si ton interlocuteur fait des erreurs
- Si ton interlocuteur fait une erreur, tu peux reformuler naturellement la phrase correcte dans ta réponse sans le corriger explicitement
- Tu expliques le vocabulaire par le contexte, pas en donnant des cours
- Tu fais référence à la vie parisienne actuelle (2026) : les quartiers, les événements, la culture
- Tes réponses sont concises comme des vrais textos (pas des dissertations)

Tu maintiens le fil de la conversation et tu te souviens de ce qui a été dit avant. Tu poses des questions pour relancer la discussion.
            """.trimIndent(),
            color = 0xFF7B68EE,
        ),
        CharacterEntity(
            id = "sarah",
            name = "Sarah",
            tag = "La Pro",
            description = "Marketing manager en startup. Ambitieuse, articulée, toujours un plan en tête. Parle boulot, voyages et bons restos.",
            location = "Montmartre, Paris",
            systemPrompt = """
Tu es Sarah, 28 ans, marketing manager dans une startup tech à Paris. Tu habites à Montmartre.

Ta personnalité :
- Ambitieuse, articulée, bien organisée
- Sociable et chaleureuse mais avec une touche de professionnalisme
- Tu utilises "tu" (entre pairs) mais ton langage est un peu plus soigné que la moyenne
- Tu adores les podcasts business, le fitness, les dégustations de vin, le networking
- Tu voyages beaucoup et tu aimes découvrir de nouveaux restos
- Tu parles souvent de tes projets, tes ambitions, et tu aimes planifier des activités

Règles de langage :
- Tu parles en français à un niveau B2+ et tu augmentes progressivement la complexité selon la compréhension de ton interlocuteur
- Tu NE fais JAMAIS de fautes d'orthographe ou de grammaire
- Tu utilises de l'argot parisien mais de façon plus mesurée : "c'est top", "ça me parle", "on se cale ça", "c'est le feu"
- Tu NE passes JAMAIS à l'anglais, même si ton interlocuteur fait des erreurs
- Si ton interlocuteur fait une erreur, tu peux reformuler naturellement la phrase correcte dans ta réponse sans le corriger explicitement
- Tu expliques le vocabulaire par le contexte, pas en donnant des cours
- Tu fais référence à la vie parisienne actuelle (2026) : les quartiers branchés, les événements, les expos
- Tes réponses sont concises comme des vrais textos

Tu maintiens le fil de la conversation et tu te souviens de ce qui a été dit avant. Tu poses des questions pour relancer la discussion.
            """.trimIndent(),
            color = 0xFFE67E22,
        ),
        CharacterEntity(
            id = "karim",
            name = "Karim",
            tag = "L'Étudiant",
            description = "Étudiant en socio, serveur à mi-temps. Passionné de foot et de jeux vidéo. Curieux, enthousiaste, plein d'énergie.",
            location = "Quartier Latin, Paris",
            systemPrompt = """
Tu es Karim, 24 ans, étudiant en master de sociologie à la Sorbonne. Tu travailles à mi-temps comme serveur dans un café du Quartier Latin.

Ta personnalité :
- Enthousiaste, énergique, curieux de tout
- Très décontracté, tu utilises beaucoup d'argot actuel
- Tu utilises "tu" (jamais "vous")
- Tu es passionné de foot (tu supportes le PSG), de jeux vidéo, de musique (rap FR, festivals)
- Tu parles souvent de la vie étudiante, de tes potes, de la fac
- Tu t'intéresses à la politique et aux questions sociales
- Tu poses beaucoup de questions sur la vie de ton interlocuteur

Règles de langage :
- Tu parles en français à un niveau B2+ et tu augmentes progressivement la complexité selon la compréhension de ton interlocuteur
- Tu NE fais JAMAIS de fautes d'orthographe ou de grammaire (tu es éduqué)
- Tu utilises beaucoup de verlan et d'argot : "c'est ouf", "wesh", "le bail", "ça craint", "j'suis deg", "c'est chanmé", "on se capte"
- Tu NE passes JAMAIS à l'anglais, même si ton interlocuteur fait des erreurs
- Si ton interlocuteur fait une erreur, tu peux reformuler naturellement la phrase correcte dans ta réponse sans le corriger explicitement
- Tu expliques le vocabulaire par le contexte, pas en donnant des cours
- Tu fais référence à la vie parisienne actuelle (2026) : les matchs du PSG, la vie étudiante, les festivals
- Tes réponses sont concises et énergiques comme des vrais textos

Tu maintiens le fil de la conversation et tu te souviens de ce qui a été dit avant. Tu poses des questions pour relancer la discussion.
            """.trimIndent(),
            color = 0xFF2ECC71,
        ),
    )
}
```

- [ ] **Step 2: Create MonPoteDatabase**

`core/database/src/main/java/com/monpote/core/database/MonPoteDatabase.kt`:
```kotlin
package com.monpote.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.monpote.core.database.dao.CharacterDao
import com.monpote.core.database.dao.ConversationDao
import com.monpote.core.database.dao.MessageDao
import com.monpote.core.database.entity.CharacterEntity
import com.monpote.core.database.entity.ConversationEntity
import com.monpote.core.database.entity.MessageEntity

@Database(
    entities = [
        CharacterEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MonPoteDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
```

- [ ] **Step 3: Create DatabaseModule (Hilt DI)**

`core/database/src/main/java/com/monpote/core/database/di/DatabaseModule.kt`:
```kotlin
package com.monpote.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.monpote.core.database.MonPoteDatabase
import com.monpote.core.database.dao.CharacterDao
import com.monpote.core.database.dao.ConversationDao
import com.monpote.core.database.dao.MessageDao
import com.monpote.core.database.seed.CharacterSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MonPoteDatabase {
        lateinit var database: MonPoteDatabase
        database = Room.databaseBuilder(
            context,
            MonPoteDatabase::class.java,
            "monpote.db",
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        database.characterDao().insertAll(CharacterSeeder.getCharacters())
                    }
                }
            })
            .build()
        return database
    }

    @Provides
    fun provideCharacterDao(database: MonPoteDatabase): CharacterDao = database.characterDao()

    @Provides
    fun provideConversationDao(database: MonPoteDatabase): ConversationDao = database.conversationDao()

    @Provides
    fun provideMessageDao(database: MonPoteDatabase): MessageDao = database.messageDao()
}
```

- [ ] **Step 4: Verify build**

```bash
./gradlew :core:database:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add core/database/
git commit -m "feat: add Room database, seeder, DAOs, and Hilt module"
```

---

## Chunk 3: Network Layer

### Task 7: Azure OpenAI API Client

**Files:**
- Create: `core/network/src/main/java/com/monpote/core/network/dto/ChatRequest.kt`
- Create: `core/network/src/main/java/com/monpote/core/network/dto/ChatResponse.kt`
- Create: `core/network/src/main/java/com/monpote/core/network/api/AzureOpenAiService.kt`
- Create: `core/network/src/main/java/com/monpote/core/network/interceptor/ApiKeyInterceptor.kt`
- Create: `core/network/src/main/java/com/monpote/core/network/di/NetworkModule.kt`

- [ ] **Step 1: Create request DTO**

`core/network/src/main/java/com/monpote/core/network/dto/ChatRequest.kt`:
```kotlin
package com.monpote.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val messages: List<ChatMessage>,
    val temperature: Double = 0.9,
    @Json(name = "max_tokens") val maxTokens: Int = 300,
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val role: String,
    val content: String,
)
```

- [ ] **Step 2: Create response DTO**

`core/network/src/main/java/com/monpote/core/network/dto/ChatResponse.kt`:
```kotlin
package com.monpote.core.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val choices: List<Choice>,
)

@JsonClass(generateAdapter = true)
data class Choice(
    val message: ChatMessage,
)
```

- [ ] **Step 3: Create Retrofit service interface**

`core/network/src/main/java/com/monpote/core/network/api/AzureOpenAiService.kt`:
```kotlin
package com.monpote.core.network.api

import com.monpote.core.network.dto.ChatRequest
import com.monpote.core.network.dto.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AzureOpenAiService {
    @POST("openai/deployments/{deployment}/chat/completions")
    suspend fun chatCompletion(
        @Path("deployment") deployment: String,
        @Query("api-version") apiVersion: String = "2024-08-01-preview",
        @Body request: ChatRequest,
    ): ChatResponse
}
```

- [ ] **Step 4: Create API key interceptor**

`core/network/src/main/java/com/monpote/core/network/interceptor/ApiKeyInterceptor.kt`:
```kotlin
package com.monpote.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("api-key", apiKey)
            .build()
        return chain.proceed(request)
    }
}
```

- [ ] **Step 5: Create NetworkModule (Hilt DI)**

`core/network/src/main/java/com/monpote/core/network/di/NetworkModule.kt`:
```kotlin
package com.monpote.core.network.di

import com.monpote.core.network.BuildConfig
import com.monpote.core.network.api.AzureOpenAiService
import com.monpote.core.network.interceptor.ApiKeyInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(BuildConfig.AZURE_OPENAI_API_KEY))
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.AZURE_OPENAI_ENDPOINT)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideAzureOpenAiService(retrofit: Retrofit): AzureOpenAiService =
        retrofit.create(AzureOpenAiService::class.java)
}
```

- [ ] **Step 6: Add BuildConfig fields to core:network build.gradle.kts**

Update `core/network/build.gradle.kts` — add inside the `android { defaultConfig { } }` block:

```kotlin
android {
    // ... existing config ...

    defaultConfig {
        minSdk = 26

        // Read from local.properties
        val properties = java.util.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        buildConfigField("String", "AZURE_OPENAI_ENDPOINT", "\"${properties.getProperty("azure.openai.endpoint", "")}\"")
        buildConfigField("String", "AZURE_OPENAI_API_KEY", "\"${properties.getProperty("azure.openai.apikey", "")}\"")
        buildConfigField("String", "AZURE_OPENAI_DEPLOYMENT", "\"${properties.getProperty("azure.openai.deployment", "")}\"")
    }

    buildFeatures {
        buildConfig = true
    }
}
```

- [ ] **Step 7: Add Azure credentials to local.properties**

Append to `local.properties` (this file is already gitignored by Android Studio):

```properties
azure.openai.endpoint=https://YOUR_RESOURCE_NAME.openai.azure.com/
azure.openai.apikey=YOUR_API_KEY
azure.openai.deployment=YOUR_DEPLOYMENT_NAME
```

Replace the placeholder values with your actual Azure OpenAI credentials.

- [ ] **Step 8: Verify build**

```bash
./gradlew :core:network:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add core/network/
git commit -m "feat: add Azure OpenAI network client with Retrofit, Moshi, and Hilt"
```

---

## Chunk 4: UI Theme + Shared Components

### Task 8: Compose Theme

**Files:**
- Create: `core/ui/src/main/java/com/monpote/core/ui/theme/Color.kt`
- Create: `core/ui/src/main/java/com/monpote/core/ui/theme/Type.kt`
- Create: `core/ui/src/main/java/com/monpote/core/ui/theme/Theme.kt`

- [ ] **Step 1: Create color palette**

`core/ui/src/main/java/com/monpote/core/ui/theme/Color.kt`:
```kotlin
package com.monpote.core.ui.theme

import androidx.compose.ui.graphics.Color

// Background
val Background = Color(0xFF0B0B1A)
val Surface = Color(0xFF1A1A2E)
val SurfaceVariant = Color(0xFF2A2A4A)

// Primary (Paris blue)
val Primary = Color(0xFF4A90D9)
val OnPrimary = Color.White

// Text
val TextPrimary = Color(0xFFE0E0E0)
val TextSecondary = Color(0xFF888888)
val TextMuted = Color(0xFF666666)

// Character colors
val LucasColor = Color(0xFF7B68EE)
val SarahColor = Color(0xFFE67E22)
val KarimColor = Color(0xFF2ECC71)

// Status
val ErrorRed = Color(0xFFE74C3C)
```

- [ ] **Step 2: Create typography**

`core/ui/src/main/java/com/monpote/core/ui/theme/Type.kt`:
```kotlin
package com.monpote.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val MonPoteTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = TextPrimary,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontSize = 14.sp,
        color = TextPrimary,
        lineHeight = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        color = TextPrimary,
        lineHeight = 19.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        color = TextSecondary,
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp,
        color = TextMuted,
    ),
)
```

- [ ] **Step 3: Create theme composable**

`core/ui/src/main/java/com/monpote/core/ui/theme/Theme.kt`:
```kotlin
package com.monpote.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed,
)

@Composable
fun MonPoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MonPoteTypography,
        content = content,
    )
}
```

- [ ] **Step 4: Verify build**

```bash
./gradlew :core:ui:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add core/ui/
git commit -m "feat: add dark theme with Paris-inspired color palette"
```

### Task 9: Shared CharacterAvatar Component

**Files:**
- Create: `core/ui/src/main/java/com/monpote/core/ui/components/CharacterAvatar.kt`

- [ ] **Step 1: Create reusable avatar composable**

`core/ui/src/main/java/com/monpote/core/ui/components/CharacterAvatar.kt`:
```kotlin
package com.monpote.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CharacterAvatar(
    initial: Char,
    color: Color,
    size: Dp = 40.dp,
    fontSize: TextUnit = 18.sp,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    ) {
        Text(
            text = initial.toString(),
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
        )
    }
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew :core:ui:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core/ui/src/main/java/com/monpote/core/ui/components/
git commit -m "feat: add CharacterAvatar reusable composable"
```

---

## Chunk 5: Onboarding Feature

### Task 10: Onboarding ViewModel

**Files:**
- Create: `feature/onboarding/src/main/java/com/monpote/feature/onboarding/OnboardingViewModel.kt`

- [ ] **Step 1: Create OnboardingViewModel**

`feature/onboarding/src/main/java/com/monpote/feature/onboarding/OnboardingViewModel.kt`:
```kotlin
package com.monpote.feature.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monpote.core.database.dao.CharacterDao
import com.monpote.core.database.dao.ConversationDao
import com.monpote.core.database.entity.ConversationEntity
import com.monpote.core.database.mapper.toDomain
import com.monpote.core.model.Character
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PreferencesKeys {
    val SELECTED_CHARACTER_ID = stringPreferencesKey("selected_character_id")
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val characterDao: CharacterDao,
    private val conversationDao: ConversationDao,
) : ViewModel() {

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    init {
        viewModelScope.launch {
            characterDao.getAllCharacters().collect { entities ->
                _characters.value = entities.map { it.toDomain() }
            }
        }
    }

    fun selectCharacter(characterId: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            // Save selection to DataStore
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.SELECTED_CHARACTER_ID] = characterId
            }

            // Create new conversation
            val now = System.currentTimeMillis()
            val conversationId = conversationDao.insert(
                ConversationEntity(
                    characterId = characterId,
                    createdAt = now,
                    lastMessageAt = now,
                )
            )

            onComplete(conversationId)
        }
    }
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew :feature:onboarding:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add feature/onboarding/
git commit -m "feat: add OnboardingViewModel with character selection and DataStore"
```

### Task 11: Onboarding Screen UI

**Files:**
- Create: `feature/onboarding/src/main/java/com/monpote/feature/onboarding/OnboardingScreen.kt`

- [ ] **Step 1: Create OnboardingScreen composable**

`feature/onboarding/src/main/java/com/monpote/feature/onboarding/OnboardingScreen.kt`:
```kotlin
package com.monpote.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.monpote.core.model.Character
import com.monpote.core.ui.components.CharacterAvatar
import com.monpote.core.ui.theme.TextMuted
import com.monpote.core.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    onCharacterSelected: (characterId: String, conversationId: Long) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val characters by viewModel.characters.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "\uD83C\uDDEB\uD83C\uDDF7",
                fontSize = 28.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mon Pote Parisien",
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choisis ton pote",
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(32.dp))

            characters.forEach { character ->
                CharacterCard(
                    character = character,
                    onClick = {
                        viewModel.selectCharacter(character.id) { conversationId ->
                            onCharacterSelected(character.id, conversationId)
                        }
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu pourras changer de pote plus tard dans les paramètres",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun CharacterCard(
    character: Character,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CharacterAvatar(
                initial = character.name.first(),
                color = Color(character.color),
                size = 52.dp,
                fontSize = 22.sp,
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(character.color).copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = character.tag,
                            color = Color(character.color),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }

                Text(
                    text = character.location,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    modifier = Modifier.padding(top = 2.dp),
                )

                Text(
                    text = character.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew :feature:onboarding:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add feature/onboarding/
git commit -m "feat: add onboarding screen with character selection cards"
```

---

## Chunk 6: Chat Feature

### Task 12: Chat UI State

**Files:**
- Create: `feature/chat/src/main/java/com/monpote/feature/chat/ChatUiState.kt`

- [ ] **Step 1: Create UI state data class**

`feature/chat/src/main/java/com/monpote/feature/chat/ChatUiState.kt`:
```kotlin
package com.monpote.feature.chat

import com.monpote.core.model.Character
import com.monpote.core.model.Message

data class ChatUiState(
    val character: Character? = null,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
```

- [ ] **Step 2: Verify build**

```bash
./gradlew :feature:chat:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add feature/chat/src/main/java/com/monpote/feature/chat/ChatUiState.kt
git commit -m "feat: add ChatUiState data class"
```

### Task 13: Chat ViewModel

**Files:**
- Create: `feature/chat/src/main/java/com/monpote/feature/chat/ChatViewModel.kt`

- [ ] **Step 1: Create ChatViewModel**

`feature/chat/src/main/java/com/monpote/feature/chat/ChatViewModel.kt`:
```kotlin
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
            // Load character
            val entity = characterDao.getCharacterById(characterId)
            if (entity != null) {
                _uiState.update { it.copy(character = entity.toDomain()) }
            }

            // Get or create conversation
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

            // Observe messages
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

            // Save user message
            messageDao.insert(
                MessageEntity(
                    conversationId = conversationId,
                    content = text.trim(),
                    role = Role.USER.value,
                    timestamp = now,
                )
            )
            conversationDao.updateLastMessageAt(conversationId, now)

            // Call AI
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
            // Build message list: system prompt + last 20 messages
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

            val assistantContent = response.choices.firstOrNull()?.message?.content
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
```

- [ ] **Step 2: Verify build**

```bash
./gradlew :feature:chat:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add feature/chat/src/main/java/com/monpote/feature/chat/ChatViewModel.kt
git commit -m "feat: add ChatViewModel with send, retry, and AI response logic"
```

### Task 14: Chat UI Components

**Files:**
- Create: `feature/chat/src/main/java/com/monpote/feature/chat/components/ChatTopBar.kt`
- Create: `feature/chat/src/main/java/com/monpote/feature/chat/components/MessageBubble.kt`
- Create: `feature/chat/src/main/java/com/monpote/feature/chat/components/ChatInput.kt`
- Create: `feature/chat/src/main/java/com/monpote/feature/chat/components/TypingIndicator.kt`

- [ ] **Step 1: Create ChatTopBar**

`feature/chat/src/main/java/com/monpote/feature/chat/components/ChatTopBar.kt`:
```kotlin
package com.monpote.feature.chat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monpote.core.model.Character
import com.monpote.core.ui.components.CharacterAvatar
import com.monpote.core.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    character: Character,
    onNewChat: () -> Unit,
    onChangeCharacter: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CharacterAvatar(
                    initial = character.name.first(),
                    color = Color(character.color),
                    size = 36.dp,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = character.location,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = TextSecondary,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Nouveau chat") },
                    onClick = {
                        menuExpanded = false
                        onNewChat()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Changer de pote") },
                    onClick = {
                        menuExpanded = false
                        onChangeCharacter()
                    },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}
```

- [ ] **Step 2: Create MessageBubble**

`feature/chat/src/main/java/com/monpote/feature/chat/components/MessageBubble.kt`:
```kotlin
package com.monpote.feature.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monpote.core.model.Character
import com.monpote.core.model.Message
import com.monpote.core.model.Role
import com.monpote.core.ui.components.CharacterAvatar
import com.monpote.core.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: Message,
    character: Character?,
) {
    val isUser = message.role == Role.USER
    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.8).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUser && character != null) {
            CharacterAvatar(
                initial = character.name.first(),
                color = Color(character.color),
                size = 28.dp,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = if (isUser) {
                RoundedCornerShape(12.dp, 0.dp, 12.dp, 12.dp)
            } else {
                RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)
            },
            color = if (isUser) Primary else MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = maxWidth),
        ) {
            Column(modifier = Modifier.padding(10.dp, 8.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    ),
                )

                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isUser) Color.White.copy(alpha = 0.5f) else Color.Gray,
                    ),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(Date(timestamp))
}
```

- [ ] **Step 3: Create ChatInput**

`feature/chat/src/main/java/com/monpote/feature/chat/components/ChatInput.kt`:
```kotlin
package com.monpote.feature.chat.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monpote.core.ui.theme.Primary
import com.monpote.core.ui.theme.SurfaceVariant
import com.monpote.core.ui.theme.TextMuted

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
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
    }
}
```

- [ ] **Step 4: Create TypingIndicator**

`feature/chat/src/main/java/com/monpote/feature/chat/components/TypingIndicator.kt`:
```kotlin
package com.monpote.feature.chat.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monpote.core.model.Character
import com.monpote.core.ui.components.CharacterAvatar

@Composable
fun TypingIndicator(character: Character?) {
    val transition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (character != null) {
            CharacterAvatar(
                initial = character.name.first(),
                color = Color(character.color),
                size = 28.dp,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                repeat(3) { index ->
                    val alpha by transition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "dot$index",
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(alpha)
                            .background(Color.Gray, CircleShape),
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 5: Verify build**

```bash
./gradlew :feature:chat:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add feature/chat/src/main/java/com/monpote/feature/chat/components/
git commit -m "feat: add chat UI components (TopBar, MessageBubble, Input, TypingIndicator)"
```

### Task 15: Chat Screen

**Files:**
- Create: `feature/chat/src/main/java/com/monpote/feature/chat/ChatScreen.kt`

- [ ] **Step 1: Create ChatScreen composable**

`feature/chat/src/main/java/com/monpote/feature/chat/ChatScreen.kt`:
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

    // Send opening message on first load
    LaunchedEffect(Unit) {
        viewModel.sendOpeningMessage()
    }

    // Auto-scroll to bottom on new messages
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
            ChatInput(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
            )
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

            // Typing indicator
            if (uiState.isLoading) {
                item {
                    TypingIndicator(character = uiState.character)
                }
            }

            // Error banner
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

- [ ] **Step 2: Verify build**

```bash
./gradlew :feature:chat:build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add feature/chat/src/main/java/com/monpote/feature/chat/ChatScreen.kt
git commit -m "feat: add ChatScreen composable with message list, input, and error handling"
```

---

## Chunk 7: App Shell + Navigation

### Task 16: App Entry Point and Navigation

**Files:**
- Create: `app/src/main/java/com/monpote/MonPoteApplication.kt`
- Create: `app/src/main/java/com/monpote/MainActivity.kt`
- Create: `app/src/main/java/com/monpote/navigation/NavGraph.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create MonPoteApplication**

`app/src/main/java/com/monpote/MonPoteApplication.kt`:
```kotlin
package com.monpote

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MonPoteApplication : Application()
```

- [ ] **Step 2: Create NavGraph**

`app/src/main/java/com/monpote/navigation/NavGraph.kt`:
```kotlin
package com.monpote.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monpote.feature.chat.ChatScreen
import com.monpote.feature.onboarding.OnboardingScreen
import com.monpote.feature.onboarding.PreferencesKeys
import com.monpote.feature.onboarding.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var startDestination by remember { mutableStateOf<String?>(null) }

    // Determine start destination
    LaunchedEffect(Unit) {
        val savedCharacterId = context.dataStore.data
            .map { it[PreferencesKeys.SELECTED_CHARACTER_ID] }
            .first()

        startDestination = if (savedCharacterId != null) {
            "chat/$savedCharacterId"
        } else {
            "onboarding"
        }
    }

    if (startDestination == null) return // Loading

    NavHost(
        navController = navController,
        startDestination = startDestination!!,
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onCharacterSelected = { characterId, conversationId ->
                    navController.navigate("chat/$characterId") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
            )
        }

        composable("chat/{characterId}") {
            ChatScreen(
                onNavigateToOnboarding = {
                    // Clear preference and navigate back
                    scope.launch {
                        context.dataStore.edit { prefs ->
                            prefs.remove(PreferencesKeys.SELECTED_CHARACTER_ID)
                        }
                    }
                    navController.navigate("onboarding") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNewConversation = { characterId, _ ->
                    navController.navigate("chat/$characterId") {
                        popUpTo("chat/$characterId") { inclusive = true }
                    }
                },
            )
        }
    }
}
```

- [ ] **Step 3: Create MainActivity**

`app/src/main/java/com/monpote/MainActivity.kt`:
```kotlin
package com.monpote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.monpote.core.ui.theme.MonPoteTheme
import com.monpote.navigation.NavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonPoteTheme {
                NavGraph()
            }
        }
    }
}
```

- [ ] **Step 4: Update AndroidManifest.xml**

`app/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".MonPoteApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Mon Pote Parisien"
        android:supportsRtl="true"
        android:theme="@style/Theme.MonPoteParisien">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.MonPoteParisien">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 5: Build full app**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/
git commit -m "feat: add app shell with Hilt, navigation, and MainActivity"
```

### Task 17: Run on Device / Emulator

- [ ] **Step 1: Add Azure credentials to local.properties**

If not already done in Task 7, ensure `local.properties` contains your real Azure OpenAI values:
```properties
azure.openai.endpoint=https://YOUR_RESOURCE_NAME.openai.azure.com/
azure.openai.apikey=YOUR_API_KEY
azure.openai.deployment=YOUR_DEPLOYMENT_NAME
```

- [ ] **Step 2: Run the app**

In Android Studio: Run → Run 'app' (or Shift+F10).

Expected behavior:
1. App launches showing the character selection screen with Lucas, Sarah, and Karim
2. Tapping a character navigates to the chat screen
3. The AI character sends an opening message automatically
4. You can type a message and receive an AI response in French
5. Messages persist — closing and reopening the app shows the conversation history
6. The overflow menu offers "Nouveau chat" and "Changer de pote"

- [ ] **Step 3: Verify error handling**

Turn off Wi-Fi/data on the device, send a message. Expected: error banner "Impossible d'envoyer. Réessayer ?" appears. Turn connectivity back on, tap the banner. Expected: message sends successfully.

- [ ] **Step 4: Final commit**

```bash
git add app/ core/ feature/ settings.gradle.kts build.gradle.kts gradle/
git commit -m "chore: finalize MVP — Mon Pote Parisien chat app"
```
