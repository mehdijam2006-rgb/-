package com.lettermanager.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lettermanager.util.SecurityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val securityPreferences: SecurityPreferences
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val hasPinSet: StateFlow<Boolean> = securityPreferences.pinHash
        .map { it != null && it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val biometricEnabled: StateFlow<Boolean> = securityPreferences.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun verifyPin(pin: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val storedHash = securityPreferences.pinHash.first()
            val inputHash = securityPreferences.hashPin(pin)
            if (storedHash == inputHash) {
                securityPreferences.updateLastActive()
                _authState.update { it.copy(isAuthenticated = true) }
                onSuccess()
            } else {
                _authState.update { it.copy(pinError = true) }
                onError()
            }
        }
    }

    fun setPin(newPin: String, confirmPin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (newPin.length < 4) {
                onError("پین باید حداقل ۴ رقم باشد")
                return@launch
            }
            if (newPin != confirmPin) {
                onError("پین‌ها مطابقت ندارند")
                return@launch
            }
            securityPreferences.setPinHash(securityPreferences.hashPin(newPin))
            onSuccess()
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityPreferences.setBiometricEnabled(enabled)
        }
    }

    fun clearPinError() {
        _authState.update { it.copy(pinError = false) }
    }
}

data class AuthState(
    val isAuthenticated: Boolean = false,
    val pinError: Boolean = false
)
