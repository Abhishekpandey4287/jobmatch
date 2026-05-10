package com.jobmatch.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// ── SessionManager ────────────────────────────────────────────────────────────

/**
 * Single source of truth for the user session.
 * Persists JWT token and phone number via DataStore so the user
 * remains logged in across app restarts.
 *
 * The token's presence (non-null) is the definition of "logged in".
 */
class SessionManager(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_PHONE = stringPreferencesKey("user_phone")
    }

    /** Cold flow — always reflects the latest persisted value. */
    val tokenFlow: Flow<String?> = dataStore.data.map { it[KEY_TOKEN] }
    val phoneFlow: Flow<String?> = dataStore.data.map { it[KEY_PHONE] }

    /** Suspending read — used by the Ktor auth plugin's [loadTokens] lambda. */
    suspend fun getToken(): String? = tokenFlow.firstOrNull()

    suspend fun saveSession(token: String, phone: String) {
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_PHONE] = phone
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}