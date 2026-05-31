package com.lettermanager.presentation.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lettermanager.service.BackupService
import com.lettermanager.util.SecurityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securityPreferences: SecurityPreferences,
    private val backupService: BackupService
) : ViewModel() {

    val biometricEnabled = securityPreferences.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val syncUrl = securityPreferences.syncUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val lockTimeout = securityPreferences.lockTimeoutMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    var statusMessage by mutableStateOf<String?>(null)
        private set

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { securityPreferences.setBiometricEnabled(enabled) }
    }

    fun setSyncUrl(url: String) {
        viewModelScope.launch { securityPreferences.setSyncUrl(url) }
    }

    fun setLockTimeout(minutes: Int) {
        viewModelScope.launch { securityPreferences.setLockTimeout(minutes) }
    }

    fun changePin(currentPin: String, newPin: String, confirmPin: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val storedHash = securityPreferences.pinHash.stateIn(viewModelScope).value
            if (storedHash != null && storedHash != securityPreferences.hashPin(currentPin)) {
                onResult("پین فعلی اشتباه است"); return@launch
            }
            if (newPin.length < 4) { onResult("پین باید حداقل ۴ رقم باشد"); return@launch }
            if (newPin != confirmPin) { onResult("پین‌ها مطابقت ندارند"); return@launch }
            securityPreferences.setPinHash(securityPreferences.hashPin(newPin))
            onResult("پین با موفقیت تغییر یافت")
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            val result = backupService.exportToJson()
            statusMessage = if (result.isSuccess) "پشتیبان‌گیری با موفقیت انجام شد"
            else "خطا در پشتیبان‌گیری: ${result.exceptionOrNull()?.message}"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val syncUrl by viewModel.syncUrl.collectAsStateWithLifecycle()
    val lockTimeout by viewModel.lockTimeout.collectAsStateWithLifecycle()
    var syncUrlInput by remember(syncUrl) { mutableStateOf(syncUrl) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    viewModel.statusMessage?.let {
        LaunchedEffect(it) {
            kotlinx.coroutines.delay(3000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "بازگشت")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Security Section
            SettingsSectionHeader("امنیت")

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(4.dp)) {
                    ListItem(
                        headlineContent = { Text("احراز هویت بیومتریک") },
                        supportingContent = { Text("ورود با اثر انگشت") },
                        leadingContent = { Icon(Icons.Default.Fingerprint, null) },
                        trailingContent = {
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { viewModel.setBiometricEnabled(it) }
                            )
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("تغییر پین") },
                        supportingContent = { Text("تغییر رمز ورود") },
                        leadingContent = { Icon(Icons.Default.Lock, null) },
                        modifier = androidx.compose.ui.Modifier.then(
                            androidx.compose.ui.Modifier
                        ),
                        trailingContent = {
                            IconButton(onClick = { showChangePinDialog = true }) {
                                Icon(Icons.Default.ArrowForward, null)
                            }
                        }
                    )
                }
            }

            // Auto-lock timeout
            SettingsSectionHeader("قفل خودکار")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("قفل بعد از ${lockTimeout} دقیقه عدم فعالیت",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = lockTimeout.toFloat(),
                        onValueChange = { viewModel.setLockTimeout(it.toInt()) },
                        valueRange = 1f..60f,
                        steps = 10
                    )
                }
            }

            // Sync Section
            SettingsSectionHeader("همگام‌سازی Google Sheets")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = syncUrlInput,
                        onValueChange = { syncUrlInput = it },
                        label = { Text("آدرس Google Apps Script") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.setSyncUrl(syncUrlInput) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ذخیره آدرس") }
                }
            }

            // Backup Section
            SettingsSectionHeader("پشتیبان‌گیری")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(4.dp)) {
                    ListItem(
                        headlineContent = { Text("صدور پشتیبان") },
                        supportingContent = { Text("ذخیره داده‌ها به فرمت JSON") },
                        leadingContent = { Icon(Icons.Default.Upload, null) },
                        trailingContent = {
                            Button(onClick = { viewModel.exportBackup() }) {
                                Text("صدور")
                            }
                        }
                    )
                }
            }

            viewModel.statusMessage?.let {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (it.contains("موفق"))
                            MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(12.dp),
                        color = if (it.contains("موفق"))
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    if (showChangePinDialog) {
        ChangePinDialog(
            onDismiss = { showChangePinDialog = false },
            onConfirm = { current, new, confirm ->
                viewModel.changePin(current, new, confirm) { msg ->
                    showChangePinDialog = false
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun ChangePinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تغییر پین") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = { currentPin = it },
                    label = { Text("پین فعلی") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("پین جدید") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    label = { Text("تکرار پین جدید") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(currentPin, newPin, confirmPin) }) { Text("تأیید") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
