package com.example.e_commerceapp.domain.repository

import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import kotlinx.coroutines.flow.Flow

interface ApiRepository {

    fun getProductsListFromApi(): Flow<List<ProductEntity>>

    fun getSingleProductByIdFromApi(productId: Int): Flow<ProductDetailsEntity>

    fun getProductsListBySearchFromApi(query: String): Flow<List<SearchProductEntity>>

    fun getAllCategoriesFromApi(): Flow<List<Category>>

    fun getProductsListByCategoryNameFromApi(categoryName: String): Flow<List<ProductEntity>>
}
