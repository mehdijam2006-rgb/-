package com.lettermanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "financialReceipts",
    foreignKeys = [
        ForeignKey(
            entity = LetterEntity::class,
            parentColumns = ["id"],
            childColumns = ["letterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("letterId")]
)
data class FinancialReceiptEntity(
    @PrimaryKey(autoGenerate = true)
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
