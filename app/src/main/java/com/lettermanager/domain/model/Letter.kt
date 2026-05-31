package com.lettermanager.domain.model

data class Letter(
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
    val status: LetterStatus = LetterStatus.ACTIVE,
    val synced: Boolean = false
)

enum class LetterStatus(val value: String) {
    ACTIVE("active"),
    ARCHIVED("archived");

    companion object {
        fun fromString(value: String) = entries.firstOrNull { it.value == value } ?: ACTIVE
    }
}
