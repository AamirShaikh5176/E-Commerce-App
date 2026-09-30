package com.example.e_commerceapp.di.repository

import com.example.e_commerceapp.data.repository.ApiRepositoryImpl
import com.example.e_commerceapp.data.repository.FirestoreCatalogRepositoryImpl
import com.example.e_commerceapp.data.repository.FirebaseRepositoryImpl
import com.example.e_commerceapp.data.repository.LocalRepositoryImpl
import com.example.e_commerceapp.domain.repository.ApiRepository
import com.example.e_commerceapp.domain.repository.FirebaseRepository
import com.example.e_commerceapp.domain.repository.LocalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds   // this fast from provide annotation
    @ViewModelScoped   // live with viewModel
    abstract fun bindFirebaseRepository(
        repository: FirebaseRepositoryImpl,
    ): FirebaseRepository
    @Binds
    @ViewModelScoped
    abstract fun bindApiRepository(
        repository: FirestoreCatalogRepositoryImpl,
    ): ApiRepository

    @Binds
    @ViewModelScoped
    abstract fun bindLocalRepository(
        repository: LocalRepositoryImpl,
    ): LocalRepository

    @Binds
    @ViewModelScoped
    abstract fun bindAddressRepository(
        repository: com.example.e_commerceapp.data.repository.AddressRepositoryImpl,
    ): com.example.e_commerceapp.domain.repository.AddressRepository

    @Binds
    @ViewModelScoped
    abstract fun bindPaymentRepository(
        repository: com.example.e_commerceapp.data.repository.PaymentRepositoryImpl,
    ): com.example.e_commerceapp.domain.repository.PaymentRepository

    @Binds
    @ViewModelScoped
    abstract fun bindSuperCoinsRepository(
        repository: com.example.e_commerceapp.data.repository.SuperCoinsRepositoryImpl,
    ): com.example.e_commerceapp.domain.repository.SuperCoinsRepository
}
