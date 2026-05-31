package com.lettermanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "letters")
data class LetterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val autoNumber: String,
    val manualLetterNumber: String? = null,
    val letterDateShamsi: String,
    val letterDateMiladi: Long,
    val sender: String,
    val deadlineShamsi: String,
    val deadlineMiladi: Long,
    val applicantName: String,
    val responseDateShamsi: String? = null,
    val responseDateMiladi: Long? = null,
    val autoResponseNumber: String? = null,
    val manualResponseNumber: String? = null,
    val status: String = "active",   // active | archived
    val synced: Boolean = false
)
