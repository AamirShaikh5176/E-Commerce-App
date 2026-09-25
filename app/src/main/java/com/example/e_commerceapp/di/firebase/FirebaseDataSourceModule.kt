package com.example.e_commerceapp.di.firebase

import com.example.e_commerceapp.data.data_source.remote.firebase.FirebaseDataSourceImpl
import com.mustafaunlu.ecommerce_compose.data.source.remote.FirebaseDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class FirebaseDataSourceModule {

    @Binds
    @ViewModelScoped
    abstract fun bindDataSource(
        dataSource: FirebaseDataSourceImpl,
    ): FirebaseDataSource
}