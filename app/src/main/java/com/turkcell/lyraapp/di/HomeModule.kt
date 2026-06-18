package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.home.HomeRepository
import com.turkcell.lyraapp.data.home.RemoteHomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Home feature'ının repository bağlamaları.
 *
 * Backend hazır olmadığından [HomeRepository], MOCK implementasyona ([MockHomeRepository])
 * bağlanır. Gerçek API geldiğinde yalnızca bu bağlamanın hedefi değiştirilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: RemoteHomeRepository): HomeRepository
}
