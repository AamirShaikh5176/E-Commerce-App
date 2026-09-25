package com.example.e_commerceapp.data.data_source.remote.firebase

import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.data.dto.firestore.FirestoreCategoryDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreProductDto
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * Remote data source interface for real-time Firestore catalog operations.
 */
interface FirestoreCatalogDataSource {
    fun getProductsFlow(): Flow<List<ProductEntity>>
    fun getProductByIdFlow(productId: String): Flow<ProductDetailsEntity?>
    fun getProductsByCategoryFlow(categoryName: String): Flow<List<ProductEntity>>
    fun searchProductsFlow(query: String): Flow<List<SearchProductEntity>>
    fun getCategoriesFlow(): Flow<List<Category>>
    suspend fun saveProduct(product: FirestoreProductDto)
    suspend fun saveProductsBatch(products: List<FirestoreProductDto>)
    suspend fun saveCategoriesBatch(categories: List<FirestoreCategoryDto>)
    suspend fun hasProducts(): Boolean
}
