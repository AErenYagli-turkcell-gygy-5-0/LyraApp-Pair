package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.preferences.DataStoreThemeRepository
import com.turkcell.lyraapp.data.preferences.ThemePreferenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Tema tercihi repository bağlaması.
 *
 * [ThemePreferenceRepository] Singleton'dır: hem [MainActivity] hem de [ProfileViewModel]
 * aynı DataStore akışını dinler; [setTheme] çağrısı tüm dinleyicilere anında yayılır.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ThemePreferenceModule {

    @Binds
    @Singleton
    abstract fun bindThemePreferenceRepository(
        impl: DataStoreThemeRepository,
    ): ThemePreferenceRepository
}
