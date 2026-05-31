package com.lettermanager.domain.model

data class SearchFilter(
    val query: String = "",
    val status: String = "",        // "" | "active" | "archived"
    val fromDateMiladi: Long = 0,
    val toDateMiladi: Long = 0
)
