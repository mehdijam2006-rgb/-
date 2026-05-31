package com.lettermanager.presentation.ui.letters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lettermanager.data.repository.FinancialRepository
import com.lettermanager.data.repository.LetterRepository
import com.lettermanager.domain.model.FinancialReceipt
import com.lettermanager.domain.model.Letter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LetterDetailViewModel @Inject constructor(
    private val letterRepository: LetterRepository,
    private val financialRepository: FinancialRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val letterId: Int = checkNotNull(savedStateHandle["letterId"])

    val letter: StateFlow<Letter?> = letterRepository.getLetterByIdFlow(letterId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val receipts: StateFlow<List<FinancialReceipt>> =
        financialRepository.getReceiptsForLetter(letterId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markReceiptReceived(receiptId: Int, dateShamsi: String, dateMiladi: Long) {
        viewModelScope.launch {
            financialRepository.markReceiptAsReceived(receiptId, dateShamsi, dateMiladi)
        }
    }

    fun deleteReceipt(receipt: FinancialReceipt) {
        viewModelScope.launch {
            financialRepository.deleteReceipt(receipt)
        }
    }
}
