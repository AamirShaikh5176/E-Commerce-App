package com.example.e_commerceapp.di.network

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Paymob network module has been replaced for the Indian Payment Architecture.
 * Payment processing runs via PaymentRepositoryModule using standard UPI Intent,
 * client publishable key ID, and secure Firebase Cloud Functions (no secrets in APK).
 */
@Module
@InstallIn(SingletonComponent::class)
object IndianPaymentNetworkModule {
    // Zero secret keys stored in client APK.
}
