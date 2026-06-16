package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.playlistdetail.MockPlaylistDetailRepository
import com.turkcell.lyraapp.data.playlistdetail.PlaylistDetailRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaylistDetailModule {

    @Binds
    @Singleton
    abstract fun bindPlaylistDetailRepository(impl: MockPlaylistDetailRepository): PlaylistDetailRepository
}
