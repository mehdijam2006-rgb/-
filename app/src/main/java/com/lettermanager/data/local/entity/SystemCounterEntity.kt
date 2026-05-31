package com.lettermanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "systemCounters")
data class SystemCounterEntity(
    @PrimaryKey
    val year: Int,
    val currentNumber: Int = 0
)
