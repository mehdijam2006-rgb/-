package com.lettermanager.data.repository

import com.lettermanager.data.local.dao.LetterDao
import com.lettermanager.data.local.entity.toDomain
import com.lettermanager.data.local.entity.toEntity
import com.lettermanager.domain.model.Letter
import com.lettermanager.domain.model.LetterStatus
import com.lettermanager.domain.model.SearchFilter
import com.lettermanager.service.NumberGeneratorService
import com.lettermanager.util.PersianCalendarUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LetterRepository @Inject constructor(
    private val letterDao: LetterDao,
    private val numberGeneratorService: NumberGeneratorService
) {
    fun getActiveLetters(): Flow<List<Letter>> =
        letterDao.getActiveLetters().map { list -> list.map { it.toDomain() } }

    fun getArchivedLetters(): Flow<List<Letter>> =
        letterDao.getArchivedLetters().map { list -> list.map { it.toDomain() } }

    suspend fun getLetterById(id: Int): Letter? =
        letterDao.getLetterById(id)?.toDomain()

    fun getLetterByIdFlow(id: Int): Flow<Letter?> =
        letterDao.getLetterByIdFlow(id).map { it?.toDomain() }

    fun getOverdueLetters(): Flow<List<Letter>> =
        letterDao.getOverdueLetters(System.currentTimeMillis()).map { list -> list.map { it.toDomain() } }

    fun searchLetters(filter: SearchFilter): Flow<List<Letter>> =
        letterDao.searchLettersFiltered(
            query = filter.query,
            status = filter.status,
            fromDate = filter.fromDateMiladi,
            toDate = filter.toDateMiladi
        ).map { list -> list.map { it.toDomain() } }

    suspend fun createLetter(
        manualLetterNumber: String?,
        letterDateShamsi: String,
        sender: String,
        deadlineShamsi: String,
        applicantName: String
    ): Letter {
        val autoNumber = numberGeneratorService.generateLetterNumber()
        val letterDate = PersianCalendarUtils.parseJalaliString(letterDateShamsi)
            ?: PersianCalendarUtils.currentJalaliDate()
        val deadlineDate = PersianCalendarUtils.parseJalaliString(deadlineShamsi)
            ?: PersianCalendarUtils.currentJalaliDate()

        val letter = Letter(
            autoNumber = autoNumber,
            manualLetterNumber = manualLetterNumber?.ifBlank { null },
            letterDateShamsi = letterDateShamsi,
            letterDateMiladi = PersianCalendarUtils.jalaliToTimestamp(letterDate.year, letterDate.month, letterDate.day),
            sender = sender,
            deadlineShamsi = deadlineShamsi,
            deadlineMiladi = PersianCalendarUtils.jalaliToTimestamp(deadlineDate.year, deadlineDate.month, deadlineDate.day),
            applicantName = applicantName,
            status = LetterStatus.ACTIVE
        )
        val id = letterDao.insert(letter.toEntity())
        return letter.copy(id = id.toInt())
    }

    suspend fun registerResponse(
        letterId: Int,
        manualResponseNumber: String?,
        responseDateShamsi: String
    ): Letter? {
        val letter = letterDao.getLetterById(letterId) ?: return null
        val autoResponseNumber = numberGeneratorService.generateResponseNumber()
        val responseDate = PersianCalendarUtils.parseJalaliString(responseDateShamsi)
            ?: PersianCalendarUtils.currentJalaliDate()

        val updated = letter.copy(
            autoResponseNumber = autoResponseNumber,
            manualResponseNumber = manualResponseNumber?.ifBlank { null },
            responseDateShamsi = responseDateShamsi,
            responseDateMiladi = PersianCalendarUtils.jalaliToTimestamp(responseDate.year, responseDate.month, responseDate.day),
            status = "archived",
            synced = false
        )
        letterDao.update(updated)
        return updated.toDomain()
    }

    suspend fun archiveLetter(letterId: Int) {
        val letter = letterDao.getLetterById(letterId) ?: return
        letterDao.update(letter.copy(status = "archived", synced = false))
    }

    suspend fun updateLetter(letter: Letter) {
        letterDao.update(letter.toEntity())
    }

    suspend fun deleteLetter(letter: Letter) {
        letterDao.delete(letter.toEntity())
    }

    suspend fun getUnsyncedLetters(): List<Letter> =
        letterDao.getUnsyncedLetters().map { it.toDomain() }

    suspend fun markLetterSynced(id: Int) {
        letterDao.markSynced(id)
    }

    suspend fun getLettersWithDeadlineBetween(from: Long, to: Long): List<Letter> =
        letterDao.getLettersWithDeadlineBetween(from, to).map { it.toDomain() }
}
