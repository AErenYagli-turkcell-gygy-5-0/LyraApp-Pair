package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.download.DownloadRepository
import com.turkcell.lyraapp.data.download.RealDownloadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DownloadModule {

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: RealDownloadRepository): DownloadRepository
}
