package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.profile.MockProfileRepository
import com.turkcell.lyraapp.data.profile.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Profil feature'ının repository bağlamaları.
 *
 * Backend hazır olmadığından [ProfileRepository], MOCK implementasyona ([MockProfileRepository])
 * bağlanır. Gerçek API geldiğinde yalnızca bu bağlamanın hedefi değiştirilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: MockProfileRepository): ProfileRepository
}
