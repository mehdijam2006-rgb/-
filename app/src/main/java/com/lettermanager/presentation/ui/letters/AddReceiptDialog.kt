package com.lettermanager.presentation.ui.letters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lettermanager.domain.model.FinancialReceipt
import com.lettermanager.presentation.components.PersianDatePickerDialog
import com.lettermanager.util.PersianCalendarUtils

@Composable
fun AddReceiptDialog(
    letterId: Int,
    onDismiss: () -> Unit,
    onConfirm: (FinancialReceipt) -> Unit
) {
    var receiptNumber by remember { mutableStateOf("") }
    var receiptDateShamsi by remember {
        mutableStateOf(PersianCalendarUtils.currentJalaliDate().toString())
    }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var receiptNumberError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("افزودن فیش بانکی", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it; receiptNumberError = false },
                    label = { Text("شماره فیش *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = receiptNumberError
                )

                OutlinedTextField(
                    value = receiptDateShamsi,
                    onValueChange = {},
                    label = { Text("تاریخ فیش *") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, null)
                        }
                    }
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it; amountError = false },
                    label = { Text("مبلغ (ریال) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات (اختیاری)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("انصراف") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        receiptNumberError = receiptNumber.isBlank()
                        amountError = amountStr.isBlank() || amountStr.toLongOrNull() == null
                        if (receiptNumberError || amountError) return@Button

                        val parsedDate = PersianCalendarUtils.parseJalaliString(receiptDateShamsi)
                            ?: PersianCalendarUtils.currentJalaliDate()
                        val receipt = FinancialReceipt(
                            letterId = letterId,
                            receiptNumber = receiptNumber,
                            receiptDateShamsi = receiptDateShamsi,
                            receiptDateMiladi = PersianCalendarUtils.jalaliToTimestamp(
                                parsedDate.year, parsedDate.month, parsedDate.day
                            ),
                            amount = amountStr.toLong(),
                            description = description.ifBlank { null }
                        )
                        onConfirm(receipt)
                    }) {
                        Text("ثبت فیش")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        PersianDatePickerDialog(
            onDateSelected = {
                receiptDateShamsi = it.toString()
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
