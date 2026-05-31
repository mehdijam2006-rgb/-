package com.lettermanager.presentation.ui.letters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lettermanager.presentation.components.PersianDatePickerDialog
import com.lettermanager.util.PersianCalendarUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLetterScreen(
    onNavigateBack: () -> Unit,
    viewModel: LettersViewModel = hiltViewModel()
) {
    var sender by remember { mutableStateOf("") }
    var applicantName by remember { mutableStateOf("") }
    var manualLetterNumber by remember { mutableStateOf("") }
    var letterDateShamsi by remember {
        mutableStateOf(PersianCalendarUtils.currentJalaliDate().toString())
    }
    var deadlineShamsi by remember {
        mutableStateOf(PersianCalendarUtils.currentJalaliDate().toString())
    }
    var showLetterDatePicker by remember { mutableStateOf(false) }
    var showDeadlinePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var senderError by remember { mutableStateOf(false) }
    var applicantError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ثبت نامه جدید") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sender
            OutlinedTextField(
                value = sender,
                onValueChange = { sender = it; senderError = false },
                label = { Text("فرستنده *") },
                modifier = Modifier.fillMaxWidth(),
                isError = senderError,
                supportingText = if (senderError) {{ Text("فرستنده الزامی است") }} else null
            )

            // Applicant
            OutlinedTextField(
                value = applicantName,
                onValueChange = { applicantName = it; applicantError = false },
                label = { Text("نام متقاضی *") },
                modifier = Modifier.fillMaxWidth(),
                isError = applicantError,
                supportingText = if (applicantError) {{ Text("نام متقاضی الزامی است") }} else null
            )

            // Manual letter number
            OutlinedTextField(
                value = manualLetterNumber,
                onValueChange = { manualLetterNumber = it },
                label = { Text("شماره دستی نامه (اختیاری)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Letter date
            OutlinedTextField(
                value = letterDateShamsi,
                onValueChange = {},
                label = { Text("تاریخ نامه *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showLetterDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "انتخاب تاریخ")
                    }
                }
            )

            // Deadline
            OutlinedTextField(
                value = deadlineShamsi,
                onValueChange = {},
                label = { Text("مهلت رسیدگی *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDeadlinePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "انتخاب تاریخ")
                    }
                }
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    senderError = sender.isBlank()
                    applicantError = applicantName.isBlank()
                    if (senderError || applicantError) return@Button

                    isLoading = true
                    viewModel.createLetter(
                        manualLetterNumber = manualLetterNumber,
                        letterDateShamsi = letterDateShamsi,
                        sender = sender,
                        deadlineShamsi = deadlineShamsi,
                        applicantName = applicantName,
                        onSuccess = {
                            isLoading = false
                            onNavigateBack()
                        },
                        onError = { msg ->
                            isLoading = false
                            errorMessage = msg
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("ذخیره نامه")
                }
            }
        }
    }

    if (showLetterDatePicker) {
        PersianDatePickerDialog(
            onDateSelected = {
                letterDateShamsi = it.toString()
                showLetterDatePicker = false
            },
            onDismiss = { showLetterDatePicker = false }
        )
    }

    if (showDeadlinePicker) {
        PersianDatePickerDialog(
            onDateSelected = {
                deadlineShamsi = it.toString()
                showDeadlinePicker = false
            },
            onDismiss = { showDeadlinePicker = false }
        )
    }
}
