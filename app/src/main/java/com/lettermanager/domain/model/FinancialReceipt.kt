package com.lettermanager.domain.model

data class FinancialReceipt(
    val id: Int = 0,
    val letterId: Int,
    val receiptNumber: String,
    val receiptDateShamsi: String,
    val receiptDateMiladi: Long,
    val amount: Long,
    val description: String? = null,
    val isReceived: Boolean = false,
    val receivedDateShamsi: String? = null,
    val receivedDateMiladi: Long? = null
)
