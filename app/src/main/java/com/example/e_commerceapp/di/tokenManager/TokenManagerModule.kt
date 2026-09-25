package com.example.e_commerceapp.di.tokenManager

import android.content.SharedPreferences
import com.example.e_commerceapp.utils.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TokenManagerModule {

    @Provides
    @Singleton
    fun provideTokenManager(
        sharedPreferences: SharedPreferences
    ): TokenManager {
        return TokenManager(sharedPreferences)
    }
}