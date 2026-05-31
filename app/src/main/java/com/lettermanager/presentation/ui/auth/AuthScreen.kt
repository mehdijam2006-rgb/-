package com.lettermanager.presentation.ui.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val hasPinSet by viewModel.hasPinSet.collectAsStateWithLifecycle()
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSetupMode by remember { mutableStateOf(!hasPinSet) }

    LaunchedEffect(hasPinSet) {
        isSetupMode = !hasPinSet
    }

    // Biometric prompt
    fun showBiometricPrompt() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onAuthSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Fall back to PIN
                }
                override fun onAuthenticationFailed() {
                    // Show error
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("احراز هویت")
            .setSubtitle("برای ورود به برنامه اثر انگشت خود را تأیید کنید")
            .setNegativeButtonText("استفاده از پین")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    LaunchedEffect(biometricEnabled, hasPinSet) {
        if (biometricEnabled && hasPinSet) {
            val canAuth = BiometricManager.from(context)
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                showBiometricPrompt()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "سامانه مدیریت نامه",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isSetupMode) "تنظیم پین ورود" else "ورود با پین",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 8) {
                    pin = it
                    errorMessage = null
                    viewModel.clearPinError()
                }
            },
            label = { Text(if (isSetupMode) "پین جدید" else "پین") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            trailingIcon = {
                IconButton(onClick = { showPin = !showPin }) {
                    Icon(
                        if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPin) "پنهان" else "نمایش"
                    )
                }
            },
            isError = authState.pinError || errorMessage != null
        )

        if (isSetupMode) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPin,
                onValueChange = { if (it.length <= 8) confirmPin = it },
                label = { Text("تکرار پین") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = errorMessage != null
            )
        }

        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (authState.pinError) {
            Spacer(Modifier.height(8.dp))
            Text("پین اشتباه است", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (isSetupMode) {
                    viewModel.setPin(
                        newPin = pin,
                        confirmPin = confirmPin,
                        onSuccess = {
                            isSetupMode = false
                            pin = ""
                            confirmPin = ""
                            onAuthSuccess()
                        },
                        onError = { errorMessage = it }
                    )
                } else {
                    viewModel.verifyPin(
                        pin = pin,
                        onSuccess = onAuthSuccess,
                        onError = { pin = "" }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(if (isSetupMode) "تنظیم پین" else "ورود")
        }

        if (!isSetupMode && biometricEnabled) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { showBiometricPrompt() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ورود با اثر انگشت")
            }
        }
    }
}
