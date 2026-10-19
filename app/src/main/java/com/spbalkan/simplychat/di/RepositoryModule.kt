package com.spbalkan.simplychat.di

import com.spbalkan.simplychat.auth.AuthRepository
import com.spbalkan.simplychat.auth.AuthRepositoryImp
import com.spbalkan.simplychat.chat.data.ChatRepository
import com.spbalkan.simplychat.chat.data.ChatRepositoryImp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindAuthRepository(
        authRepositoryImp: AuthRepositoryImp
    ): AuthRepository

    @Binds
    @Singleton
    fun bindChatRepository(
        chatRepositoryImp: ChatRepositoryImp
    ): ChatRepository
}