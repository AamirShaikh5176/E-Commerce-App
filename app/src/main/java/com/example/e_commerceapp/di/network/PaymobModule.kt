package com.example.e_commerceapp.di.network

import com.example.e_commerceapp.data.data_source.remote.api.PaymobApiService
import com.example.e_commerceapp.utils.Constants.PAYMOB_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymobModule {

    @Provides
    @Singleton
    fun providePaymobApi(): PaymobApiService {
        return Retrofit.Builder()
            .baseUrl(PAYMOB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymobApiService::class.java)
    }
}
