package com.example.e_commerceapp.di.repository

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * PaymentRepository is now bound in RepositoryModule with @ViewModelScoped.
 */
@Module
@InstallIn(SingletonComponent::class)
object PaymentRepositoryModule {
    // Deprecated - bindings moved to RepositoryModule
}
