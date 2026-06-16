package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.createplaylist.CreatePlaylistRepository
import com.turkcell.lyraapp.data.createplaylist.MockCreatePlaylistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CreatePlaylistModule {

    @Binds
    @Singleton
    abstract fun bindCreatePlaylistRepository(impl: MockCreatePlaylistRepository): CreatePlaylistRepository
}
