package com.example.e_commerceapp.data.repository

import com.example.e_commerceapp.data.data_source.remote.api.ApiService
import com.example.e_commerceapp.data.data_source.remote.firebase.FirestoreCatalogDataSource
import com.example.e_commerceapp.data.data_source.remote.migrator.CatalogDataMigrator
import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.data.mapper.toDetailsProductEntity
import com.example.e_commerceapp.data.mapper.toListProductEntity
import com.example.e_commerceapp.data.mapper.toListSearchProductEntity
import com.example.e_commerceapp.di.coroutine.IoDispatcher
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import com.example.e_commerceapp.domain.repository.ApiRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Implementation of ApiRepository backed by Cloud Firestore real-time snapshot listeners.
 * Seamlessly integrates with existing domain use cases while delivering real-time updates
 * and automatic catalog seeding via CatalogDataMigrator.
 */
class FirestoreCatalogRepositoryImpl @Inject constructor(
    private val firestoreCatalogDataSource: FirestoreCatalogDataSource,
    private val catalogDataMigrator: CatalogDataMigrator,
    private val apiService: ApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ApiRepository {

    override fun getProductsListFromApi(): Flow<List<ProductEntity>> = flow {
        // Trigger background catalog migration if Firestore is currently unpopulated
        try {
            catalogDataMigrator.migrateCatalogIfNeeded()
        } catch (_: Exception) {
        }

        firestoreCatalogDataSource.getProductsFlow().collect { firestoreList ->
            if (firestoreList.isNotEmpty()) {
                emit(firestoreList)
            } else {
                // Graceful fallback while migration is executing
                try {
                    val apiProducts = apiService.getProductsListFromApi().products.toListProductEntity()
                    emit(apiProducts)
                } catch (_: Exception) {
                    emit(emptyList())
                }
            }
        }
    }.flowOn(ioDispatcher)

    override fun getSingleProductByIdFromApi(productId: String): Flow<ProductDetailsEntity> = flow {
        firestoreCatalogDataSource.getProductByIdFlow(productId).collect { product ->
            if (product != null) {
                emit(product)
            } else {
                try {
                    val apiProduct = apiService.getSingleProductByIdFromApi(productId).toDetailsProductEntity()
                    emit(apiProduct)
                } catch (e: Exception) {
                    // Propagate error if product cannot be retrieved
                    throw e
                }
            }
        }
    }.flowOn(ioDispatcher)

    override fun getProductsListBySearchFromApi(query: String): Flow<List<SearchProductEntity>> = flow {
        firestoreCatalogDataSource.searchProductsFlow(query).collect { results ->
            if (results.isNotEmpty()) {
                emit(results)
            } else {
                try {
                    val apiResults = apiService.getProductsListBySearchFromApi(query).products.toListSearchProductEntity()
                    emit(apiResults)
                } catch (_: Exception) {
                    emit(emptyList())
                }
            }
        }
    }.flowOn(ioDispatcher)

    override fun getAllCategoriesFromApi(): Flow<List<Category>> = flow {
        firestoreCatalogDataSource.getCategoriesFlow().collect { categories ->
            if (categories.isNotEmpty()) {
                emit(categories)
            } else {
                try {
                    val apiCategories = apiService.getAllCategoriesFromApi()
                    emit(apiCategories)
                } catch (_: Exception) {
                    emit(emptyList())
                }
            }
        }
    }.flowOn(ioDispatcher)

    override fun getProductsListByCategoryNameFromApi(categoryName: String): Flow<List<ProductEntity>> = flow {
        firestoreCatalogDataSource.getProductsByCategoryFlow(categoryName).collect { products ->
            if (products.isNotEmpty()) {
                emit(products)
            } else {
                try {
                    val apiProducts = apiService.getProductsListByCategoryNameFromApi(categoryName).products.toListProductEntity()
                    emit(apiProducts)
                } catch (_: Exception) {
                    emit(emptyList())
                }
            }
        }
    }.flowOn(ioDispatcher)
}
