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

    private fun createBackendOkHttpClient(baseClient: OkHttpClient): OkHttpClient {
        return baseClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                val originalUrl = request.url

                val candidateUrls = mutableListOf(originalUrl)
                if (originalUrl.host != "10.111.231.179") {
                    candidateUrls.add(originalUrl.newBuilder().scheme("http").host("10.111.231.179").port(5000).build())
                }
                if (originalUrl.host != "10.0.2.2") {
                    candidateUrls.add(originalUrl.newBuilder().scheme("http").host("10.0.2.2").port(5000).build())
                }
                if (originalUrl.host != "127.0.0.1") {
                    candidateUrls.add(originalUrl.newBuilder().scheme("http").host("127.0.0.1").port(5000).build())
                }

                var lastException: java.io.IOException? = null
                for (targetUrl in candidateUrls) {
                    val newRequest = request.newBuilder().url(targetUrl).build()
                    try {
                        val response = chain.proceed(newRequest)
                        if (response.code != 404 && response.code != 502) {
                            return@addInterceptor response
                        }
                    } catch (e: java.io.IOException) {
                        lastException = e
                    }
                }
                throw lastException ?: java.io.IOException("Unable to reach payment server")
            }
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRazorpayApiService(okHttpClient: OkHttpClient): com.example.e_commerceapp.data.data_source.remote.api.RazorpayApiService {
        val backendClient = createBackendOkHttpClient(okHttpClient)
        return Retrofit.Builder()
            .baseUrl(com.example.e_commerceapp.utils.Constants.BACKEND_BASE_URL)
            .client(backendClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(com.example.e_commerceapp.data.data_source.remote.api.RazorpayApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWalletApiService(okHttpClient: OkHttpClient): com.example.e_commerceapp.data.data_source.remote.api.WalletApiService {
        val backendClient = createBackendOkHttpClient(okHttpClient)
        return Retrofit.Builder()
            .baseUrl(com.example.e_commerceapp.utils.Constants.BACKEND_BASE_URL)
            .client(backendClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(com.example.e_commerceapp.data.data_source.remote.api.WalletApiService::class.java)
    }
}






