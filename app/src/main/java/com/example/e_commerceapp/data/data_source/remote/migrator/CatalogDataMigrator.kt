package com.example.e_commerceapp.data.data_source.remote.migrator

import android.util.Log
import com.example.e_commerceapp.data.data_source.remote.api.ApiService
import com.example.e_commerceapp.data.data_source.remote.firebase.FirestoreCatalogDataSource
import com.example.e_commerceapp.data.dto.firestore.FirestoreCategoryDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreProductDto
import com.example.e_commerceapp.di.coroutine.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToLong

import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Migration engine that seeds or synchronizes the read-only catalog API (DummyJSON)
 * into Firestore collections (/products and /categories) with normalized INR pricing,
 * MRP, and Indian e-commerce metadata.
 */
@ViewModelScoped
class CatalogDataMigrator @Inject constructor(
    private val apiService: ApiService,
    private val firestoreCatalogDataSource: FirestoreCatalogDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val tag = "CatalogDataMigrator"

    /**
     * Checks if Firestore /products has documents. If empty, automatically
     * migrates the remote catalog into Firestore.
     */
    suspend fun migrateCatalogIfNeeded(): Result<Int> = withContext(ioDispatcher) {
        try {
            val hasData = firestoreCatalogDataSource.hasProducts()
            if (hasData) {
                Log.d(tag, "Firestore catalog already populated. Migration skipped.")
                return@withContext Result.success(0)
            }
            Log.d(tag, "Firestore catalog is empty. Starting initial catalog migration...")
            migrateCatalog(force = true)
        } catch (e: Exception) {
            Log.e(tag, "Error checking catalog migration state: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches products and categories from DummyJSON, normalizes prices to INR (₹),
     * calculates realistic MRP, and batch-writes to Firestore collections.
     */
    suspend fun migrateCatalog(force: Boolean = false): Result<Int> = withContext(ioDispatcher) {
        try {
            // 1. Fetch categories and seed /categories
            val apiCategories = try {
                apiService.getAllCategoriesFromApi()
            } catch (e: Exception) {
                Log.w(tag, "Could not fetch categories from API: ${e.message}")
                emptyList()
            }

            if (apiCategories.isNotEmpty()) {
                val firestoreCategories = apiCategories.map { cat ->
                    FirestoreCategoryDto(
                        id = cat.slug,
                        name = cat.name,
                        slug = cat.slug,
                        url = cat.url,
                    )
                }
                firestoreCatalogDataSource.saveCategoriesBatch(firestoreCategories)
                Log.d(tag, "Migrated ${firestoreCategories.size} categories to Firestore.")
            }

            // 2. Fetch products and convert to Indian marketplace format
            val productsResponse = apiService.getProductsListFromApi()
            val apiProducts = productsResponse.products

            val firestoreProducts = apiProducts.map { dto ->
                // Normalized INR price conversion (80 INR per USD approx rate)
                val inrPrice = (dto.price * 80.0).roundToLong().toDouble()
                val mrpPrice = if (dto.discountPercentage > 0) {
                    (inrPrice * (1.0 + (dto.discountPercentage / 100.0))).roundToLong().toDouble()
                } else {
                    (inrPrice * 1.35).roundToLong().toDouble()
                }.coerceAtLeast(inrPrice)

                FirestoreProductDto(
                    id = dto.id.toString(),
                    title = dto.title,
                    description = dto.description,
                    price = inrPrice,
                    mrpPrice = mrpPrice,
                    discountPercentage = dto.discountPercentage,
                    categoryId = dto.category,
                    categoryName = dto.category.replaceFirstChar { it.uppercase() },
                    images = dto.images,
                    imageUrl = dto.images.firstOrNull() ?: dto.thumbnail,
                    stockQuantity = dto.stock.coerceAtLeast(15),
                    inStock = dto.stock > 0,
                    rating = dto.rating,
                    reviewsCount = dto.reviews.size.coerceAtLeast(24),
                    brand = dto.brand.ifBlank { "Generic" },
                    tags = dto.tags,
                    deliveryDays = 2,
                    isCodAvailable = true,
                    quantity = dto.minimumOrderQuantity.coerceAtLeast(1),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            }

            firestoreCatalogDataSource.saveProductsBatch(firestoreProducts)
            Log.d(tag, "Successfully migrated ${firestoreProducts.size} products to Firestore in INR format.")

            Result.success(firestoreProducts.size)
        } catch (e: Exception) {
            Log.e(tag, "Catalog migration failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
