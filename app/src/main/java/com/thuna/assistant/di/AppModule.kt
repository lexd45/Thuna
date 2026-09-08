package com.thuna.assistant.di

import android.content.Context
import com.thuna.assistant.data.repository.AssistantRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAssistantRepository(
        @ApplicationContext context: Context
    ): AssistantRepository {
        return AssistantRepository(context)
    }
}
