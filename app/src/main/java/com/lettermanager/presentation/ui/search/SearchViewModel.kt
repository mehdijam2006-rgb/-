package com.lettermanager.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lettermanager.data.repository.LetterRepository
import com.lettermanager.domain.model.Letter
import com.lettermanager.domain.model.SearchFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val letterRepository: LetterRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(SearchFilter())
    val filter: StateFlow<SearchFilter> = _filter.asStateFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        observeSearch()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _filter
                .debounce(300L)
                .flatMapLatest { filter ->
                    letterRepository.searchLetters(filter)
                }
                .collect { results ->
                    _uiState.update { it.copy(results = results, isLoading = false) }
                }
        }
    }

    fun updateQuery(query: String) {
        _filter.update { it.copy(query = query) }
        _uiState.update { it.copy(isLoading = true) }
    }

    fun updateStatus(status: String) {
        _filter.update { it.copy(status = status) }
    }

    fun updateDateRange(from: Long, to: Long) {
        _filter.update { it.copy(fromDateMiladi = from, toDateMiladi = to) }
    }

    fun clearFilters() {
        _filter.value = SearchFilter()
    }
}

data class SearchUiState(
    val results: List<Letter> = emptyList(),
    val isLoading: Boolean = false
)
