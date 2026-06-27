package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.membership.MembershipRepository
import com.turkcell.lyraapp.data.membership.RealMembershipRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MembershipModule {

    @Binds
    @Singleton
    abstract fun bindMembershipRepository(impl: RealMembershipRepository): MembershipRepository
}
