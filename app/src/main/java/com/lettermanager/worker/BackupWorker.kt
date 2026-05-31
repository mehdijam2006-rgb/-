package com.lettermanager.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.GsonBuilder
import com.lettermanager.data.local.AppDatabase
import com.lettermanager.data.local.entity.FinancialReceiptEntity
import com.lettermanager.data.local.entity.LetterEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class BackupDataDto(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val activeLetters: List<LetterEntity>,
    val archivedLetters: List<LetterEntity>,
    val receipts: List<FinancialReceiptEntity>
)

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val appDatabase: AppDatabase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(applicationContext.filesDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val activeLetters   = appDatabase.letterDao().getActiveLetters().first()
            val archivedLetters = appDatabase.letterDao().getArchivedLetters().first()
            val receipts        = appDatabase.financialReceiptDao().getAllReceipts().first()

            val gson = GsonBuilder().setPrettyPrinting().create()
            val data = BackupDataDto(
                activeLetters   = activeLetters,
                archivedLetters = archivedLetters,
                receipts        = receipts
            )

            val dateStr    = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
            val backupFile = File(backupDir, "backup_$dateStr.json")
            backupFile.writeText(gson.toJson(data))

            // Keep only last 7 backups
            backupDir.listFiles()
                ?.filter { it.name.startsWith("backup_") }
                ?.sortedByDescending { it.lastModified() }
                ?.drop(7)
                ?.forEach { it.delete() }

            Log.i("BackupWorker", "Backup created: ${backupFile.name}")
            Result.success()
        } catch (e: Exception) {
            Log.e("BackupWorker", "Backup failed: ${e.message}", e)
            Result.failure()
        }
    }
}
