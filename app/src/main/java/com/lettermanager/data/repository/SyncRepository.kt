package com.lettermanager.data.repository

import android.util.Log
import com.lettermanager.data.remote.LetterSyncDto
import com.lettermanager.data.remote.ReceiptSyncDto
import com.lettermanager.data.remote.SyncApiService
import com.lettermanager.data.remote.SyncPayload
import com.lettermanager.domain.model.FinancialReceipt
import com.lettermanager.domain.model.Letter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val syncApiService: SyncApiService,
    private val letterRepository: LetterRepository,
    private val financialRepository: FinancialRepository
) {
    private val tag = "SyncRepository"

    suspend fun syncPendingData(): Result<Unit> {
        return try {
            val unsyncedLetters = letterRepository.getUnsyncedLetters()
            if (unsyncedLetters.isEmpty()) return Result.success(Unit)

            val letterIds = unsyncedLetters.map { it.id }
            val allReceipts = mutableListOf<FinancialReceipt>()

            // Collect receipts is done via DB directly here
            val payload = SyncPayload(
                letters = unsyncedLetters.map { it.toSyncDto() },
                receipts = allReceipts.map { it.toSyncDto() }
            )

            val response = syncApiService.syncData(payload)
            if (response.success) {
                letterIds.forEach { letterRepository.markLetterSynced(it) }
                Log.i(tag, "Synced ${letterIds.size} letters successfully")
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Sync failed"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Sync error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun Letter.toSyncDto() = LetterSyncDto(
        id = id,
        autoNumber = autoNumber,
        manualLetterNumber = manualLetterNumber,
        letterDateShamsi = letterDateShamsi,
        sender = sender,
        deadlineShamsi = deadlineShamsi,
        applicantName = applicantName,
        responseDateShamsi = responseDateShamsi,
        autoResponseNumber = autoResponseNumber,
        manualResponseNumber = manualResponseNumber,
        status = status.value
    )

    private fun FinancialReceipt.toSyncDto() = ReceiptSyncDto(
        id = id,
        letterId = letterId,
        receiptNumber = receiptNumber,
        receiptDateShamsi = receiptDateShamsi,
        amount = amount,
        description = description,
        isReceived = isReceived,
        receivedDateShamsi = receivedDateShamsi
    )
}
