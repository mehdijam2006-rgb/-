package com.lettermanager.service

import android.content.Context
import com.google.gson.GsonBuilder
import com.lettermanager.data.local.AppDatabase
import com.lettermanager.data.local.entity.FinancialReceiptEntity
import com.lettermanager.data.local.entity.LetterEntity
import com.lettermanager.worker.BackupDataDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val backupDir get() = File(context.filesDir, "backups").also { it.mkdirs() }

    suspend fun exportToJson(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val activeLetters   = appDatabase.letterDao().getActiveLetters().first()
            val archivedLetters = appDatabase.letterDao().getArchivedLetters().first()
            val receipts        = appDatabase.financialReceiptDao().getAllReceipts().first()

            val data = BackupDataDto(
                activeLetters   = activeLetters,
                archivedLetters = archivedLetters,
                receipts        = receipts
            )

            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file    = File(backupDir, "backup_$dateStr.json")
            file.writeText(gson.toJson(data))

            backupDir.listFiles()
                ?.filter { it.name.startsWith("backup_") && it.name.endsWith(".json") }
                ?.sortedByDescending { it.lastModified() }
                ?.drop(7)
                ?.forEach { it.delete() }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromJson(jsonContent: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = gson.fromJson(jsonContent, BackupDataDto::class.java)
                ?: return@withContext Result.failure(Exception("فرمت پشتیبان نامعتبر است"))

            data.activeLetters.forEach   { appDatabase.letterDao().insert(it) }
            data.archivedLetters.forEach { appDatabase.letterDao().insert(it) }
            data.receipts.forEach        { appDatabase.financialReceiptDao().insert(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("خطا در بازیابی: ${e.message}"))
        }
    }

    fun listBackups(): List<File> =
        backupDir.listFiles()
            ?.filter { it.name.startsWith("backup_") && it.name.endsWith(".json") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
}
