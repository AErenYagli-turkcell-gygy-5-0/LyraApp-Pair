package com.turkcell.lyraapp.data.auth

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

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "lyra_auth",
)

class AuthTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    val accessToken: Flow<String?> = context.authDataStore.data
        .map { prefs -> prefs[accessTokenKey] }

    val isLoggedIn: Flow<Boolean> = context.authDataStore.data
        .map { prefs -> prefs[accessTokenKey] != null }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.authDataStore.edit { prefs ->
            prefs[accessTokenKey] = accessToken
            prefs[refreshTokenKey] = refreshToken
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }
}
