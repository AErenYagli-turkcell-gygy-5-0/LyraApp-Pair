package com.turkcell.lyraapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "lyra_theme",
)

/**
 * [ThemePreferenceRepository]'nin DataStore implementasyonu.
 *
 * Tercih `lyra_theme` DataStore dosyasında `is_dark_theme` anahtarıyla saklanır.
 * [isDarkTheme] Flow'u DataStore değiştiğinde otomatik güncellenir; ek emit gerekmez.
 */
class DataStoreThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ThemePreferenceRepository {

    private val isDarkKey = booleanPreferencesKey("is_dark_theme")

    override val isDarkTheme: Flow<Boolean> = context.themeDataStore.data
        .map { prefs -> prefs[isDarkKey] ?: false }

    override suspend fun setTheme(isDark: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[isDarkKey] = isDark
        }
    }
}
