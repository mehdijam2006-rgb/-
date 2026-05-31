package com.lettermanager.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lettermanager.util.PersianCalendarUtils

@Composable
fun PersianDatePickerDialog(
    initialDate: PersianCalendarUtils.PersianDate = PersianCalendarUtils.currentJalaliDate(),
    onDateSelected: (PersianCalendarUtils.PersianDate) -> Unit,
    onDismiss: () -> Unit
) {
    var displayYear by remember { mutableStateOf(initialDate.year) }
    var displayMonth by remember { mutableStateOf(initialDate.month) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: month/year navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (displayMonth == 1) {
                            displayMonth = 12; displayYear--
                        } else displayMonth--
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "قبلی")
                    }
                    Text(
                        text = "${PersianCalendarUtils.persianMonthName(displayMonth)} $displayYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        if (displayMonth == 12) {
                            displayMonth = 1; displayYear++
                        } else displayMonth++
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بعدی")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Day-of-week headers (Persian: Sat to Fri)
                val dayHeaders = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                Row(Modifier.fillMaxWidth()) {
                    dayHeaders.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Compute first day offset (Gregorian Calendar for day-of-week)
                val (gy, gm, gd) = PersianCalendarUtils.jalaliToGregorian(displayYear, displayMonth, 1)
                val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Tehran"))
                cal.set(gy, gm - 1, gd)
                // Persian week starts on Saturday (7 in Calendar -> index 0)
                val calDow = cal.get(java.util.Calendar.DAY_OF_WEEK)
                val offset = when (calDow) {
                    java.util.Calendar.SATURDAY -> 0
                    java.util.Calendar.SUNDAY -> 1
                    java.util.Calendar.MONDAY -> 2
                    java.util.Calendar.TUESDAY -> 3
                    java.util.Calendar.WEDNESDAY -> 4
                    java.util.Calendar.THURSDAY -> 5
                    java.util.Calendar.FRIDAY -> 6
                    else -> 0
                }
                val daysInMonth = PersianCalendarUtils.daysInJalaliMonth(displayYear, displayMonth)
                val cells = offset + daysInMonth

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(220.dp)
                ) {
                    items(cells) { index ->
                        if (index < offset) {
                            Box(modifier = Modifier.size(36.dp))
                        } else {
                            val day = index - offset + 1
                            val isSelected = selectedDate.year == displayYear &&
                                    selectedDate.month == displayMonth &&
                                    selectedDate.day == day
                            val isToday = run {
                                val today = PersianCalendarUtils.currentJalaliDate()
                                today.year == displayYear && today.month == displayMonth && today.day == day
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> androidx.compose.ui.graphics.Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        selectedDate = PersianCalendarUtils.PersianDate(displayYear, displayMonth, day)
                                    }
                            ) {
                                Text(
                                    text = day.toPersianDigits(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onDateSelected(selectedDate) }) { Text("تأیید") }
                }
            }
        }
    }
}

fun Int.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.toString().map { c ->
        if (c in '0'..'9') persianDigits[c - '0'] else c
    }.joinToString("")
}

fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.map { c ->
        if (c in '0'..'9') persianDigits[c - '0'] else c
    }.joinToString("")
}
