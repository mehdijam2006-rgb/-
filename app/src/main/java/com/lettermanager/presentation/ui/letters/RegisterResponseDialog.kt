package com.lettermanager.presentation.ui.letters

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lettermanager.presentation.components.PersianDatePickerDialog
import com.lettermanager.util.PersianCalendarUtils

@Composable
fun RegisterResponseDialog(
    letterId: Int,
    onDismiss: () -> Unit,
    onConfirm: (manualResponseNumber: String, responseDateShamsi: String) -> Unit
) {
    var manualResponseNumber by remember { mutableStateOf("") }
    var responseDateShamsi by remember {
        mutableStateOf(PersianCalendarUtils.currentJalaliDate().toString())
    }
    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("ثبت پاسخ نامه", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = manualResponseNumber,
                    onValueChange = { manualResponseNumber = it },
                    label = { Text("شماره دستی پاسخ (اختیاری)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = responseDateShamsi,
                    onValueChange = {},
                    label = { Text("تاریخ پاسخ *") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "انتخاب تاریخ")
                        }
                    }
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirm(manualResponseNumber, responseDateShamsi) }) {
                        Text("ثبت پاسخ و بایگانی")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        PersianDatePickerDialog(
            onDateSelected = {
                responseDateShamsi = it.toString()
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
