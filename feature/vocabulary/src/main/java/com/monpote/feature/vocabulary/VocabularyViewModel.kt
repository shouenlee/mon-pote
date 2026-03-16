package com.monpote.feature.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monpote.core.database.dao.SavedWordDao
import com.monpote.core.database.entity.SavedWordEntity
import com.monpote.core.database.mapper.toDomain
import com.monpote.core.model.SavedWord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val savedWordDao: SavedWordDao,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val words: StateFlow<List<SavedWord>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                savedWordDao.getAllWords()
            } else {
                savedWordDao.searchWords(query)
            }
        }
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun toggleReinforcement(wordId: Long, enabled: Boolean) {
        viewModelScope.launch {
            savedWordDao.updateReinforcement(wordId, enabled)
        }
    }

    fun deleteWord(wordId: Long) {
        viewModelScope.launch {
            savedWordDao.delete(wordId)
        }
    }
}
