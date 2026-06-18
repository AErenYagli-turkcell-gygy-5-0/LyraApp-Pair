package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.playback.ExoPlayerPlaybackRepository
import com.turkcell.lyraapp.data.playback.PlaybackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaybackModule {

    @Binds
    @Singleton
    abstract fun bindPlaybackRepository(impl: ExoPlayerPlaybackRepository): PlaybackRepository
}
