package com.example.e_commerceapp.data.data_source.remote.firebase

import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.data.dto.firestore.FirestoreCategoryDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreProductDto
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Remote data source interface for real-time Firestore catalog operations and admin management.
 */
interface FirestoreCatalogDataSource {
    fun getProductsFlow(): Flow<List<ProductEntity>>
    fun getRawProductsFlow(): Flow<List<FirestoreProductDto>>
    fun getProductByIdFlow(productId: String): Flow<ProductDetailsEntity?>
    fun getRawProductByIdFlow(productId: String): Flow<FirestoreProductDto?>
    fun getProductsByCategoryFlow(categoryName: String): Flow<List<ProductEntity>>
    fun searchProductsFlow(query: String): Flow<List<SearchProductEntity>>
    fun getCategoriesFlow(): Flow<List<Category>>
    fun getRawCategoriesFlow(): Flow<List<FirestoreCategoryDto>>
    suspend fun saveProduct(product: FirestoreProductDto)
    suspend fun deleteProduct(productId: String)
    suspend fun updateProductStock(productId: String, inStock: Boolean, stockQuantity: Int? = null)
    suspend fun saveCategory(category: FirestoreCategoryDto)
    suspend fun deleteCategory(categoryId: String)
    suspend fun saveProductsBatch(products: List<FirestoreProductDto>)
    suspend fun saveCategoriesBatch(categories: List<FirestoreCategoryDto>)
    suspend fun hasProducts(): Boolean
    suspend fun hasCategories(): Boolean

    // Customer & Admin Orders & Users
    fun getOrdersFlow(): Flow<List<FirestoreOrderDto>>
    fun getUserOrdersFlow(userId: String): Flow<List<FirestoreOrderDto>>
    fun getOrderByIdFlow(orderId: String): Flow<FirestoreOrderDto?>
    suspend fun updateOrderStatus(orderId: String, status: String, trackingNumber: String = "", courierPartner: String = "")
    suspend fun updateOrderStatusAndPayment(orderId: String, status: String, paymentStatus: String? = null, trackingNumber: String = "", courierPartner: String = "")
    fun getUsersFlow(): Flow<List<UserInformationEntity>>
}
