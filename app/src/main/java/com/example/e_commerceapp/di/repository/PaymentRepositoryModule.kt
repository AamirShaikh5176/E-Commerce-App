package com.example.e_commerceapp.di.repository

import com.example.e_commerceapp.data.data_source.remote.api.PaymobApiService
import com.example.e_commerceapp.data.repository.PaymentRepositoryImpl
import com.example.e_commerceapp.domain.repository.PaymentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentRepositoryModule {

    @Provides
    @Singleton
    fun providePaymentRepository(
        api: PaymobApiService
    ): PaymentRepository {
        return PaymentRepositoryImpl(api)
    }
}
