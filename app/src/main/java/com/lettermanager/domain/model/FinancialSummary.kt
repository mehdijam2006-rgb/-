package com.lettermanager.domain.model

data class FinancialSummary(
    val totalReceived: Long,
    val totalUnpaid: Long,
    val monthlyData: List<MonthlyFinancialData>
)

data class MonthlyFinancialData(
    val yearMonth: String,       // e.g. "1405/فروردین"
    val received: Long,
    val unpaid: Long
)
