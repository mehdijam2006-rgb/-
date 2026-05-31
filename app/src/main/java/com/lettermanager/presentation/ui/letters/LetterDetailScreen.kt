package com.lettermanager.presentation.ui.letters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lettermanager.domain.model.LetterStatus
import com.lettermanager.presentation.components.InfoRow
import com.lettermanager.presentation.components.formatAmount
import com.lettermanager.presentation.ui.financial.ReceiptCard
import com.lettermanager.util.PersianCalendarUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: LetterDetailViewModel = hiltViewModel()
) {
    val letter by viewModel.letter.collectAsStateWithLifecycle()
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(letter?.autoNumber ?: "جزئیات نامه") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "بازگشت")
                    }
                }
            )
        }
    ) { paddingValues ->
        letter?.let { l ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Letter Info Card
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "اطلاعات نامه",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            if (l.status == LetterStatus.ACTIVE) "فعال" else "بایگانی",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (l.status == LetterStatus.ACTIVE)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            InfoRow("شماره اتوماتیک", l.autoNumber)
                            l.manualLetterNumber?.let { InfoRow("شماره دستی", it) }
                            InfoRow("تاریخ نامه", l.letterDateShamsi)
                            InfoRow("فرستنده", l.sender)
                            InfoRow("متقاضی", l.applicantName)
                            InfoRow("مهلت رسیدگی", l.deadlineShamsi)

                            val isOverdue = l.status == LetterStatus.ACTIVE &&
                                    l.deadlineMiladi < System.currentTimeMillis()
                            if (isOverdue) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "مهلت رسیدگی گذشته است",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Response Info Card
                if (l.status == LetterStatus.ARCHIVED) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "اطلاعات پاسخ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(Modifier.height(12.dp))
                                l.autoResponseNumber?.let {
                                    InfoRow("شماره پاسخ اتوماتیک", it)
                                }
                                l.manualResponseNumber?.let {
                                    InfoRow("شماره پاسخ دستی", it)
                                }
                                l.responseDateShamsi?.let {
                                    InfoRow("تاریخ پاسخ", it)
                                }
                            }
                        }
                    }
                }

                // Financial Section
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "فیش‌های بانکی (${receipts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (receipts.isNotEmpty()) {
                            val totalUnpaid = receipts.filter { !it.isReceived }.sumOf { it.amount }
                            if (totalUnpaid > 0) {
                                Text(
                                    "باقیمانده: ${totalUnpaid.formatAmount()} ریال",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    "تسویه کامل",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                if (receipts.isEmpty()) {
                    item {
                        Text(
                            "هیچ فیشی ثبت نشده است",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(receipts, key = { it.id }) { receipt ->
                        ReceiptCard(
                            receipt = receipt,
                            showMarkReceived = !receipt.isReceived,
                            onMarkReceived = {
                                val today = PersianCalendarUtils.currentJalaliDate()
                                viewModel.markReceiptReceived(
                                    receipt.id,
                                    today.toString(),
                                    System.currentTimeMillis()
                                )
                            },
                            onDelete = { viewModel.deleteReceipt(receipt) }
                        )
                    }
                }
            }
        } ?: Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
