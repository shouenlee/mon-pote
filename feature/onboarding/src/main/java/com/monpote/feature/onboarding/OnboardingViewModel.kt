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
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.SELECTED_CHARACTER_ID] = characterId
            }

            // Resume existing conversation if one exists, otherwise create new
            val existing = conversationDao.getLatestConversation(characterId)
            val conversationId = if (existing != null) {
                existing.id
            } else {
                val now = System.currentTimeMillis()
                conversationDao.insert(
                    ConversationEntity(
                        characterId = characterId,
                        createdAt = now,
                        lastMessageAt = now,
                    )
                )
            }

            onComplete(conversationId)
        }
    }
}
