package com.turkcell.lyraapp.data.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Tema tercihinin veri kaynağı soyutlaması.
 *
 * [isDarkTheme] Flow'u hem [MainActivity] (uygulama geneli tema) hem de
 * [ProfileViewModel] (toggle gösterimi) tarafından toplanır.
 * [setTheme] çağrısı Flow'u anında günceller; tüm dinleyiciler değişimden haberdar olur.
 */
interface ThemePreferenceRepository {

    /** Mevcut tema tercihini yayan Flow; uygulama başlangıcında `false` (Light) döner. */
    val isDarkTheme: Flow<Boolean>

    /** Temayı kalıcı olarak günceller. */
    suspend fun setTheme(isDark: Boolean)
}
