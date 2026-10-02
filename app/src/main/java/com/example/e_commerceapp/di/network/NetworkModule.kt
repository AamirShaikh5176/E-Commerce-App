package com.example.e_commerceapp.di.network

import com.example.e_commerceapp.data.data_source.remote.api.ApiService
import com.example.e_commerceapp.utils.Constants.BASE_URL_API_PRODUCT
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }).build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_API_PRODUCT)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRazorpayApiService(okHttpClient: OkHttpClient): com.example.e_commerceapp.data.data_source.remote.api.RazorpayApiService {
        return Retrofit.Builder()
            .baseUrl(com.example.e_commerceapp.utils.Constants.BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(com.example.e_commerceapp.data.data_source.remote.api.RazorpayApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWalletApiService(okHttpClient: OkHttpClient): com.example.e_commerceapp.data.data_source.remote.api.WalletApiService {
        return Retrofit.Builder()
            .baseUrl(com.example.e_commerceapp.utils.Constants.BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(com.example.e_commerceapp.data.data_source.remote.api.WalletApiService::class.java)
    }
}






