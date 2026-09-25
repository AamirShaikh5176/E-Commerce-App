package com.example.e_commerceapp.di.repository

import com.example.e_commerceapp.data.repository.PaymentRepositoryImpl
import com.example.e_commerceapp.domain.repository.LocalRepository
import com.example.e_commerceapp.domain.repository.PaymentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        localRepository: LocalRepository,
    ): PaymentRepository {
        return PaymentRepositoryImpl(firestore, auth, localRepository)
    }
}
