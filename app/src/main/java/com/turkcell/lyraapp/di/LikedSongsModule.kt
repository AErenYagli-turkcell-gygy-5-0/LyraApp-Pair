package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.likedsongs.LikedSongsRepository
import com.turkcell.lyraapp.data.likedsongs.MockLikedSongsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Beğenilen Şarkılar feature'ının repository bağlamaları.
 *
 * Backend hazır olmadığından [LikedSongsRepository], MOCK implementasyona
 * ([MockLikedSongsRepository]) bağlanır. Gerçek API geldiğinde yalnızca bu bağlamanın
 * hedefi değiştirilir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LikedSongsModule {

    @Binds
    @Singleton
    abstract fun bindLikedSongsRepository(impl: MockLikedSongsRepository): LikedSongsRepository
}
