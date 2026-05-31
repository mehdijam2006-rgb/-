package com.lettermanager.presentation.ui.letters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lettermanager.data.repository.FinancialRepository
import com.lettermanager.data.repository.LetterRepository
import com.lettermanager.domain.model.FinancialReceipt
import com.lettermanager.domain.model.Letter
import com.lettermanager.service.NotificationScheduler
import com.lettermanager.util.PersianCalendarUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LettersViewModel @Inject constructor(
    private val letterRepository: LetterRepository,
    private val financialRepository: FinancialRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(LettersUiState())
    val uiState: StateFlow<LettersUiState> = _uiState.asStateFlow()

    init {
        loadActiveLetters()
    }

    private fun loadActiveLetters() {
        viewModelScope.launch {
            combine(
                letterRepository.getActiveLetters(),
                letterRepository.getOverdueLetters()
            ) { active, overdue ->
                val overdueIds = overdue.map { it.id }.toSet()
                active to overdueIds
            }.collect { (letters, overdueIds) ->
                _uiState.update {
                    it.copy(
                        letters = letters,
                        overdueIds = overdueIds,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createLetter(
        manualLetterNumber: String,
        letterDateShamsi: String,
        sender: String,
        deadlineShamsi: String,
        applicantName: String,
        onSuccess: (Letter) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val letter = letterRepository.createLetter(
                    manualLetterNumber = manualLetterNumber,
                    letterDateShamsi = letterDateShamsi,
                    sender = sender,
                    deadlineShamsi = deadlineShamsi,
                    applicantName = applicantName
                )
                notificationScheduler.scheduleDeadlineNotifications(letter)
                onSuccess(letter)
            } catch (e: Exception) {
                onError(e.message ?: "خطا در ذخیره نامه")
            }
        }
    }

    fun registerResponse(
        letterId: Int,
        manualResponseNumber: String,
        responseDateShamsi: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                letterRepository.registerResponse(letterId, manualResponseNumber, responseDateShamsi)
                notificationScheduler.cancelDeadlineNotifications(letterId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "خطا در ثبت پاسخ")
            }
        }
    }

    fun archiveLetter(letterId: Int) {
        viewModelScope.launch {
            letterRepository.archiveLetter(letterId)
            notificationScheduler.cancelDeadlineNotifications(letterId)
        }
    }

    fun addReceipt(receipt: FinancialReceipt, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                financialRepository.addReceipt(receipt)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "خطا در ثبت فیش")
            }
        }
    }
}

data class LettersUiState(
    val letters: List<Letter> = emptyList(),
    val overdueIds: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
