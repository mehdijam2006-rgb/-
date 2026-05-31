package com.lettermanager.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lettermanager.domain.model.Letter
import com.lettermanager.domain.model.LetterStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterCard(
    letter: Letter,
    receiptCount: Int = 0,
    unpaidAmount: Long = 0,
    isOverdue: Boolean = false,
    onClick: () -> Unit,
    onRegisterResponse: (() -> Unit)? = null,
    onAddReceipt: (() -> Unit)? = null,
    onArchive: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = letter.autoNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    letter.manualLetterNumber?.let { manual ->
                        Text(
                            text = "شماره دستی: $manual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOverdue) {
                        AssistChip(
                            onClick = {},
                            label = { Text("مهلت گذشته", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    if (letter.status == LetterStatus.ARCHIVED) {
                        AssistChip(
                            onClick = {},
                            label = { Text("بایگانی", style = MaterialTheme.typography.labelSmall) }
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "گزینه‌ها")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            onRegisterResponse?.let {
                                DropdownMenuItem(
                                    text = { Text("ثبت پاسخ") },
                                    leadingIcon = { Icon(Icons.Default.Reply, null) },
                                    onClick = { showMenu = false; it() }
                                )
                            }
                            onAddReceipt?.let {
                                DropdownMenuItem(
                                    text = { Text("افزودن فیش") },
                                    leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                                    onClick = { showMenu = false; it() }
                                )
                            }
                            onArchive?.let {
                                DropdownMenuItem(
                                    text = { Text("بایگانی") },
                                    leadingIcon = { Icon(Icons.Outlined.Archive, null) },
                                    onClick = { showMenu = false; it() }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))

            // Info rows
            InfoRow(label = "متقاضی", value = letter.applicantName)
            InfoRow(label = "فرستنده", value = letter.sender)
            InfoRow(label = "تاریخ نامه", value = letter.letterDateShamsi)
            InfoRow(label = "مهلت", value = letter.deadlineShamsi, valueColor =
                if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )

            letter.responseDateShamsi?.let {
                InfoRow(label = "تاریخ پاسخ", value = it)
            }

            // Financial indicator
            if (receiptCount > 0 || unpaidAmount > 0) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (unpaidAmount > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (unpaidAmount > 0) "مبلغ پرداخت‌نشده: ${unpaidAmount.formatAmount()} ریال"
                        else "تسویه شده",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (unpaidAmount > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor
        )
    }
}

fun Long.formatAmount(): String {
    return String.format("%,d", this)
}
