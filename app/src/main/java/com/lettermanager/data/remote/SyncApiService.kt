package com.lettermanager.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApiService {
    @POST("exec")
    suspend fun syncData(@Body payload: SyncPayload): SyncResponse
}

data class SyncPayload(
    val action: String = "sync",
    val letters: List<LetterSyncDto>,
    val receipts: List<ReceiptSyncDto>
)

data class LetterSyncDto(
    val id: Int,
    val autoNumber: String,
    val manualLetterNumber: String?,
    val letterDateShamsi: String,
    val sender: String,
    val deadlineShamsi: String,
    val applicantName: String,
    val responseDateShamsi: String?,
    val autoResponseNumber: String?,
    val manualResponseNumber: String?,
    val status: String
)

data class ReceiptSyncDto(
    val id: Int,
    val letterId: Int,
    val receiptNumber: String,
    val receiptDateShamsi: String,
    val amount: Long,
    val description: String?,
    val isReceived: Boolean,
    val receivedDateShamsi: String?
)

data class SyncResponse(
    val success: Boolean,
    val message: String? = null
)
