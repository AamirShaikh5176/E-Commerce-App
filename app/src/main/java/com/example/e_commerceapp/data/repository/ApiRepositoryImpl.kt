package com.example.e_commerceapp.data.repository

import com.example.e_commerceapp.data.data_source.remote.api.ApiService
import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.data.mapper.toDetailsProductEntity
import com.example.e_commerceapp.data.mapper.toListProductEntity
import com.example.e_commerceapp.data.mapper.toListSearchProductEntity
import com.example.e_commerceapp.di.coroutine.IoDispatcher
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import com.example.e_commerceapp.domain.repository.ApiRepository
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ApiRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ApiRepository {


    override fun getProductsListFromApi(): Flow<List<ProductEntity>> = flow {
        val response = apiService.getProductsListFromApi()

        emit(response.products.toListProductEntity())
    }.flowOn(ioDispatcher)


    override fun getSingleProductByIdFromApi(productId: Int): Flow<ProductDetailsEntity> = flow {
        val response = apiService.getSingleProductByIdFromApi(productId)

        // convert ProductDto to DetailProductEntity
        emit(response.toDetailsProductEntity())
    }.flowOn(ioDispatcher)

    override fun getProductsListBySearchFromApi(query: String): Flow<List<SearchProductEntity>> = flow {
        val response = apiService.getProductsListBySearchFromApi(query)

        emit(response.products.toListSearchProductEntity())
    }.flowOn(ioDispatcher)

    override fun getAllCategoriesFromApi(): Flow<List<Category>> = flow {
        val response = apiService.getAllCategoriesFromApi()
        emit(response)
    }

    override fun getProductsListByCategoryNameFromApi(categoryName: String): Flow<List<ProductEntity>> =
        flow {
            val response = apiService.getProductsListByCategoryNameFromApi(categoryName)

            emit(response.products.toListProductEntity())
        }.flowOn(ioDispatcher)
}