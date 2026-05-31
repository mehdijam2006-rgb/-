package com.lettermanager.presentation.ui.letters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lettermanager.presentation.components.LetterCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveLettersScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: LettersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showResponseDialog by remember { mutableStateOf<Int?>(null) }
    var showReceiptDialog by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نامه‌های فعال") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "نامه جدید")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.letters.isEmpty() -> EmptyLettersPlaceholder()
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.letters, key = { it.id }) { letter ->
                            val isOverdue = letter.id in uiState.overdueIds
                            LetterCard(
                                letter = letter,
                                isOverdue = isOverdue,
                                onClick = { onNavigateToDetail(letter.id) },
                                onRegisterResponse = { showResponseDialog = letter.id },
                                onAddReceipt = { showReceiptDialog = letter.id },
                                onArchive = { viewModel.archiveLetter(letter.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Response Dialog
    showResponseDialog?.let { letterId ->
        RegisterResponseDialog(
            letterId = letterId,
            onDismiss = { showResponseDialog = null },
            onConfirm = { manualNumber, dateStr ->
                viewModel.registerResponse(
                    letterId = letterId,
                    manualResponseNumber = manualNumber,
                    responseDateShamsi = dateStr,
                    onSuccess = { showResponseDialog = null },
                    onError = {}
                )
            }
        )
    }

    // Receipt Dialog
    showReceiptDialog?.let { letterId ->
        AddReceiptDialog(
            letterId = letterId,
            onDismiss = { showReceiptDialog = null },
            onConfirm = { receipt ->
                viewModel.addReceipt(
                    receipt = receipt,
                    onSuccess = { showReceiptDialog = null },
                    onError = {}
                )
            }
        )
    }
}

@Composable
fun EmptyLettersPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "نامه‌ای ثبت نشده است",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "برای ثبت نامه جدید از دکمه + استفاده کنید",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
