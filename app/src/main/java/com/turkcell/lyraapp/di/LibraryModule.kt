package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.library.LibraryRepository
import com.turkcell.lyraapp.data.library.MockLibraryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Kütüphane feature'ının repository bağlamaları.
 *
 * Backend hazır olmadığından [LibraryRepository], MOCK implementasyona ([MockLibraryRepository])
 * bağlanır. Gerçek API geldiğinde yalnızca bu bağlamanın hedefi değiştirilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LibraryModule {

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: MockLibraryRepository): LibraryRepository
}
