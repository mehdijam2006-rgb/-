package com.lettermanager.service

import com.lettermanager.data.local.dao.SystemCounterDao
import com.lettermanager.data.local.entity.SystemCounterEntity
import com.lettermanager.util.PersianCalendarUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NumberGeneratorService @Inject constructor(
    private val systemCounterDao: SystemCounterDao
) {
    private val mutex = Mutex()

    /**
     * Generates the next auto letter number in format: {year}/{incrementalNumber}
     * e.g., 1405/0012
     * Thread-safe and atomic.
     */
    suspend fun generateLetterNumber(): String = mutex.withLock {
        val currentYear = PersianCalendarUtils.currentJalaliYear()
        val counter = systemCounterDao.getCounter(currentYear)
        val nextNumber = (counter?.currentNumber ?: 0) + 1
        if (counter == null) {
            systemCounterDao.insert(SystemCounterEntity(year = currentYear, currentNumber = nextNumber))
        } else {
            systemCounterDao.updateCounter(currentYear, nextNumber)
        }
        formatLetterNumber(currentYear, nextNumber)
    }

    /**
     * Generates the next auto response number.
     * Uses a separate counter namespace (negative year offset trick).
     */
    suspend fun generateResponseNumber(): String = mutex.withLock {
        val currentYear = PersianCalendarUtils.currentJalaliYear()
        val responseCounterYear = -(currentYear) // separate namespace
        val counter = systemCounterDao.getCounter(responseCounterYear)
        val nextNumber = (counter?.currentNumber ?: 0) + 1
        if (counter == null) {
            systemCounterDao.insert(SystemCounterEntity(year = responseCounterYear, currentNumber = nextNumber))
        } else {
            systemCounterDao.updateCounter(responseCounterYear, nextNumber)
        }
        "پ/${formatLetterNumber(currentYear, nextNumber)}"
    }

    private fun formatLetterNumber(year: Int, number: Int): String {
        return "$year/${number.toString().padStart(4, '0')}"
    }
}
