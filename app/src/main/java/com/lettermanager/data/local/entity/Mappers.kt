package com.lettermanager.data.local.entity

import com.lettermanager.domain.model.FinancialReceipt
import com.lettermanager.domain.model.Letter
import com.lettermanager.domain.model.LetterStatus

fun LetterEntity.toDomain() = Letter(
    id = id,
    autoNumber = autoNumber,
    manualLetterNumber = manualLetterNumber,
    letterDateShamsi = letterDateShamsi,
    letterDateMiladi = letterDateMiladi,
    sender = sender,
    deadlineShamsi = deadlineShamsi,
    deadlineMiladi = deadlineMiladi,
    applicantName = applicantName,
    responseDateShamsi = responseDateShamsi,
    responseDateMiladi = responseDateMiladi,
    autoResponseNumber = autoResponseNumber,
    manualResponseNumber = manualResponseNumber,
    status = LetterStatus.fromString(status),
    synced = synced
)

fun Letter.toEntity() = LetterEntity(
    id = id,
    autoNumber = autoNumber,
    manualLetterNumber = manualLetterNumber,
    letterDateShamsi = letterDateShamsi,
    letterDateMiladi = letterDateMiladi,
    sender = sender,
    deadlineShamsi = deadlineShamsi,
    deadlineMiladi = deadlineMiladi,
    applicantName = applicantName,
    responseDateShamsi = responseDateShamsi,
    responseDateMiladi = responseDateMiladi,
    autoResponseNumber = autoResponseNumber,
    manualResponseNumber = manualResponseNumber,
    status = status.value,
    synced = synced
)

fun FinancialReceiptEntity.toDomain() = FinancialReceipt(
    id = id,
    letterId = letterId,
    receiptNumber = receiptNumber,
    receiptDateShamsi = receiptDateShamsi,
    receiptDateMiladi = receiptDateMiladi,
    amount = amount,
    description = description,
    isReceived = isReceived,
    receivedDateShamsi = receivedDateShamsi,
    receivedDateMiladi = receivedDateMiladi
)

fun FinancialReceipt.toEntity() = FinancialReceiptEntity(
    id = id,
    letterId = letterId,
    receiptNumber = receiptNumber,
    receiptDateShamsi = receiptDateShamsi,
    receiptDateMiladi = receiptDateMiladi,
    amount = amount,
    description = description,
    isReceived = isReceived,
    receivedDateShamsi = receivedDateShamsi,
    receivedDateMiladi = receivedDateMiladi
)
