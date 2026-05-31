package com.lettermanager.data.local.dao

import androidx.room.*
import com.lettermanager.data.local.entity.FinancialReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: FinancialReceiptEntity): Long

    @Update
    suspend fun update(receipt: FinancialReceiptEntity)

    @Delete
    suspend fun delete(receipt: FinancialReceiptEntity)

    @Query("SELECT * FROM financialReceipts WHERE letterId = :letterId ORDER BY receiptDateMiladi DESC")
    fun getReceiptsForLetter(letterId: Int): Flow<List<FinancialReceiptEntity>>

    @Query("SELECT * FROM financialReceipts WHERE id = :id")
    suspend fun getReceiptById(id: Int): FinancialReceiptEntity?

    @Query("SELECT * FROM financialReceipts ORDER BY receiptDateMiladi DESC")
    fun getAllReceipts(): Flow<List<FinancialReceiptEntity>>

    @Query("SELECT * FROM financialReceipts WHERE isReceived = 0 ORDER BY receiptDateMiladi ASC")
    fun getUnpaidReceipts(): Flow<List<FinancialReceiptEntity>>

    @Query("SELECT * FROM financialReceipts WHERE isReceived = 1 ORDER BY receivedDateMiladi DESC")
    fun getReceivedReceipts(): Flow<List<FinancialReceiptEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM financialReceipts WHERE isReceived = 1")
    fun getTotalReceivedAmount(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM financialReceipts WHERE isReceived = 0")
    fun getTotalUnpaidAmount(): Flow<Long>

    @Query("""
        SELECT * FROM financialReceipts 
        WHERE receiptDateMiladi >= :fromDate AND receiptDateMiladi <= :toDate
        ORDER BY receiptDateMiladi DESC
    """)
    fun getReceiptsByDateRange(fromDate: Long, toDate: Long): Flow<List<FinancialReceiptEntity>>
}
