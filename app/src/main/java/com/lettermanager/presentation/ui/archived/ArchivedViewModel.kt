package com.lettermanager.presentation.ui.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lettermanager.data.repository.LetterRepository
import com.lettermanager.domain.model.Letter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedViewModel @Inject constructor(
    private val letterRepository: LetterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchivedUiState())
    val uiState: StateFlow<ArchivedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            letterRepository.getArchivedLetters().collect { letters ->
                _uiState.update { it.copy(letters = letters, isLoading = false) }
            }
        }
    }
}

data class ArchivedUiState(
    val letters: List<Letter> = emptyList(),
    val isLoading: Boolean = true
)
