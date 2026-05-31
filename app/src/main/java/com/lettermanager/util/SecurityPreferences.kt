package com.lettermanager.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_prefs")

@Singleton
class SecurityPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.securityDataStore

    companion object {
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LOCK_TIMEOUT_MINUTES = intPreferencesKey("lock_timeout_minutes")
        val LAST_ACTIVE_TIMESTAMP = longPreferencesKey("last_active_timestamp")
        val SYNC_URL = stringPreferencesKey("sync_url")
        val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
    }

    val pinHash: Flow<String?> = dataStore.data.map { it[PIN_HASH] }
    val biometricEnabled: Flow<Boolean> = dataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }
    val lockTimeoutMinutes: Flow<Int> = dataStore.data.map { it[LOCK_TIMEOUT_MINUTES] ?: 5 }
    val syncUrl: Flow<String> = dataStore.data.map { it[SYNC_URL] ?: "" }

    suspend fun setPinHash(hash: String) {
        dataStore.edit { it[PIN_HASH] = hash }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setLockTimeout(minutes: Int) {
        dataStore.edit { it[LOCK_TIMEOUT_MINUTES] = minutes }
    }

    suspend fun updateLastActive() {
        dataStore.edit { it[LAST_ACTIVE_TIMESTAMP] = System.currentTimeMillis() }
    }

    suspend fun setSyncUrl(url: String) {
        dataStore.edit { it[SYNC_URL] = url }
    }

    fun hashPin(pin: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest((pin + "salt_letter_mgr").toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
