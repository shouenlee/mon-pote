package com.monpote.navigation

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

    if (startDestination == null) return

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
