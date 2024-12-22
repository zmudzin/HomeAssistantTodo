package com.example.homeassistanttodo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "server_settings")

@Singleton
class ServerPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val lastServerUrl = stringPreferencesKey("last_server_url")
    private val lastToken = stringPreferencesKey("last_token")

    val serverUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[lastServerUrl] ?: ""
    }

    val token: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[lastToken] ?: ""
    }

    suspend fun saveServerSettings(serverUrl: String, token: String) {
        context.dataStore.edit { preferences ->
            preferences[lastServerUrl] = serverUrl
            preferences[lastToken] = token
        }
    }
}