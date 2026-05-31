package com.lettermanager.presentation.ui.financial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lettermanager.data.repository.FinancialRepository
import com.lettermanager.domain.model.FinancialReceipt
import com.lettermanager.domain.model.FinancialSummary
import com.lettermanager.util.PersianCalendarUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinancialViewModel @Inject constructor(
    private val financialRepository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancialUiState())
    val uiState: StateFlow<FinancialUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                financialRepository.getUnpaidReceipts(),
                financialRepository.getReceivedReceipts(),
                financialRepository.getFinancialSummary()
            ) { unpaid, received, summary ->
                FinancialUiState(
                    unpaidReceipts = unpaid,
                    receivedReceipts = received,
                    summary = summary,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun markAsReceived(receiptId: Int) {
        viewModelScope.launch {
            val today = PersianCalendarUtils.currentJalaliDate()
            financialRepository.markReceiptAsReceived(
                receiptId = receiptId,
                receivedDateShamsi = today.toString(),
                receivedDateMiladi = System.currentTimeMillis()
            )
        }
    }

    fun deleteReceipt(receipt: FinancialReceipt) {
        viewModelScope.launch {
            financialRepository.deleteReceipt(receipt)
        }
    }

    fun updateReceipt(receipt: FinancialReceipt, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                financialRepository.updateReceipt(receipt)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "خطا در بروزرسانی")
            }
        }
    }
}

data class FinancialUiState(
    val unpaidReceipts: List<FinancialReceipt> = emptyList(),
    val receivedReceipts: List<FinancialReceipt> = emptyList(),
    val summary: FinancialSummary? = null,
    val isLoading: Boolean = true
)
