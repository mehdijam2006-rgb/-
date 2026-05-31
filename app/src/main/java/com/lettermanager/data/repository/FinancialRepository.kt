package com.lettermanager.data.repository

import com.lettermanager.data.local.dao.FinancialReceiptDao
import com.lettermanager.data.local.entity.toDomain
import com.lettermanager.data.local.entity.toEntity
import com.lettermanager.domain.model.FinancialReceipt
import com.lettermanager.domain.model.FinancialSummary
import com.lettermanager.domain.model.MonthlyFinancialData
import com.lettermanager.util.PersianCalendarUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialRepository @Inject constructor(
    private val receiptDao: FinancialReceiptDao
) {
    fun getReceiptsForLetter(letterId: Int): Flow<List<FinancialReceipt>> =
        receiptDao.getReceiptsForLetter(letterId).map { it.map { e -> e.toDomain() } }

    fun getAllReceipts(): Flow<List<FinancialReceipt>> =
        receiptDao.getAllReceipts().map { it.map { e -> e.toDomain() } }

    fun getUnpaidReceipts(): Flow<List<FinancialReceipt>> =
        receiptDao.getUnpaidReceipts().map { it.map { e -> e.toDomain() } }

    fun getReceivedReceipts(): Flow<List<FinancialReceipt>> =
        receiptDao.getReceivedReceipts().map { it.map { e -> e.toDomain() } }

    fun getTotalReceivedAmount(): Flow<Long> = receiptDao.getTotalReceivedAmount()
    fun getTotalUnpaidAmount(): Flow<Long> = receiptDao.getTotalUnpaidAmount()

    suspend fun addReceipt(receipt: FinancialReceipt): FinancialReceipt {
        val id = receiptDao.insert(receipt.toEntity())
        return receipt.copy(id = id.toInt())
    }

    suspend fun updateReceipt(receipt: FinancialReceipt) {
        receiptDao.update(receipt.toEntity())
    }

    suspend fun deleteReceipt(receipt: FinancialReceipt) {
        receiptDao.delete(receipt.toEntity())
    }

    suspend fun markReceiptAsReceived(receiptId: Int, receivedDateShamsi: String, receivedDateMiladi: Long) {
        val receipt = receiptDao.getReceiptById(receiptId) ?: return
        receiptDao.update(
            receipt.copy(
                isReceived = true,
                receivedDateShamsi = receivedDateShamsi,
                receivedDateMiladi = receivedDateMiladi
            )
        )
    }

    fun getFinancialSummary(): Flow<FinancialSummary> {
        return combine(
            receiptDao.getTotalReceivedAmount(),
            receiptDao.getTotalUnpaidAmount(),
            receiptDao.getAllReceipts()
        ) { totalReceived, totalUnpaid, allReceipts ->
            val monthlyMap = mutableMapOf<String, Pair<Long, Long>>()
            allReceipts.forEach { receipt ->
                val persian = PersianCalendarUtils.timestampToJalali(receipt.receiptDateMiladi)
                val key = "${persian.year}/${PersianCalendarUtils.persianMonthName(persian.month)}"
                val current = monthlyMap.getOrDefault(key, Pair(0L, 0L))
                if (receipt.isReceived) {
                    monthlyMap[key] = current.copy(first = current.first + receipt.amount)
                } else {
                    monthlyMap[key] = current.copy(second = current.second + receipt.amount)
                }
            }
            val monthlyData = monthlyMap.entries
                .sortedBy { it.key }
                .map { (key, value) ->
                    MonthlyFinancialData(
                        yearMonth = key,
                        received = value.first,
                        unpaid = value.second
                    )
                }
            FinancialSummary(
                totalReceived = totalReceived,
                totalUnpaid = totalUnpaid,
                monthlyData = monthlyData
            )
        }
    }
}
