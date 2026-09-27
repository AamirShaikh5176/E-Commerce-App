package com.example.e_commerceapp.data.data_source.remote.migrator

import android.util.Log
import com.example.e_commerceapp.data.data_source.remote.api.ApiService
import com.example.e_commerceapp.data.data_source.remote.firebase.FirestoreCatalogDataSource
import com.example.e_commerceapp.data.demo.DemoCatalogData
import com.example.e_commerceapp.data.dto.firestore.FirestoreCategoryDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreProductDto
import com.example.e_commerceapp.di.coroutine.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToLong
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Migration engine that seeds or synchronizes the catalog into Firestore collections
 * (/products and /categories) with canonical category taxonomy and INR pricing.
 */
@ViewModelScoped
class CatalogDataMigrator @Inject constructor(
    private val apiService: ApiService,
    private val firestoreCatalogDataSource: FirestoreCatalogDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val tag = "CatalogDataMigrator"

    /**
     * Checks if Firestore /products has documents. If empty or contains old incomplete catalog (< 48),
     * automatically migrates the full authoritative catalog into Firestore.
     */
    suspend fun migrateCatalogIfNeeded(): Result<Int> = withContext(ioDispatcher) {
        try {
            val count = firestoreCatalogDataSource.getProductsCount()
            val hasCategories = firestoreCatalogDataSource.hasCategories()
            if (count >= 48 && hasCategories) {
                Log.d(tag, "Firestore catalog already has $count products and categories. Migration skipped.")
                return@withContext Result.success(count)
            }
            Log.d(tag, "Firestore catalog needs seed/update (productCount=$count, hasCategories=$hasCategories). Starting migration...")
            migrateCatalog(force = true)
        } catch (e: Exception) {
            Log.e(tag, "Error checking catalog migration state: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun mapApiCategoryToCanonical(apiCategory: String): Pair<String, String> {
        val clean = apiCategory.trim().lowercase()
        return when {
            clean in listOf("smartphones", "mobiles", "phone", "phones") -> Pair("mobiles", "Mobiles")
            clean in listOf("laptops", "tablets", "electronics") -> Pair("electronics", "Electronics")
            clean in listOf("mens-shirts", "mens-shoes", "womens-dresses", "womens-shoes", "tops", "fashion") -> Pair("fashion", "Fashion")
            clean in listOf("beauty", "fragrances", "skin-care", "skincare") -> Pair("beauty", "Beauty")
            clean in listOf("furniture", "home-decoration", "kitchen-accessories", "home") -> Pair("home", "Home")
            clean in listOf("groceries", "grocery") -> Pair("groceries", "Groceries")
            clean in listOf("sports-accessories", "sports") -> Pair("sports", "Sports")
            clean in listOf("mens-watches", "womens-watches", "sunglasses", "mobile-accessories", "womens-bags", "womens-jewellery", "accessories") -> Pair("accessories", "Accessories")
            else -> Pair("electronics", "Electronics")
        }
    }

    /**
     * Fetches products and categories, normalizes prices to INR (₹), maps to canonical
     * category taxonomy, and batch-writes to Firestore collections.
     */
    suspend fun migrateCatalog(force: Boolean = false): Result<Int> = withContext(ioDispatcher) {
        try {
            // 1. Seed canonical categories into /categories
            val categoriesToSeed = DemoCatalogData.demoCategoryDtos
            firestoreCatalogDataSource.saveCategoriesBatch(categoriesToSeed)
            Log.d(tag, "Migrated ${categoriesToSeed.size} canonical categories to Firestore.")

            // 2. Fetch products from API first
            val productsResponse = try {
                apiService.getProductsListFromApi()
            } catch (e: Exception) {
                Log.w(tag, "Could not fetch products from remote API: ${e.message}")
                null
            }
            val apiProducts = productsResponse?.products ?: emptyList()

            val firestoreProducts = if (apiProducts.size >= 48) {
                apiProducts.map { dto ->
                    val (canonicalId, canonicalName) = mapApiCategoryToCanonical(dto.category)
                    val inrPrice = (dto.price * 80.0).roundToLong().toDouble()
                    val mrpPrice = if (dto.discountPercentage > 0) {
                        (inrPrice * (1.0 + (dto.discountPercentage / 100.0))).roundToLong().toDouble()
                    } else {
                        (inrPrice * 1.35).roundToLong().toDouble()
                    }.coerceAtLeast(inrPrice)

                    val normalizedTags = (dto.tags + listOf(canonicalId, canonicalName.lowercase())).distinct()

                    FirestoreProductDto(
                        id = dto.id.toString(),
                        title = dto.title,
                        description = dto.description,
                        price = inrPrice,
                        mrpPrice = mrpPrice,
                        discountPercentage = dto.discountPercentage,
                        categoryId = canonicalId,
                        categoryName = canonicalName,
                        images = dto.images,
                        imageUrl = dto.images.firstOrNull() ?: dto.thumbnail,
                        stockQuantity = dto.stock.coerceAtLeast(15),
                        inStock = dto.stock > 0,
                        rating = dto.rating,
                        reviewsCount = dto.reviews.size.coerceAtLeast(24),
                        brand = dto.brand.ifBlank { "Generic" },
                        tags = normalizedTags,
                        deliveryDays = 2,
                        isCodAvailable = true,
                        quantity = dto.minimumOrderQuantity.coerceAtLeast(1),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                }
            } else {
                DemoCatalogData.demoProducts
            }

            firestoreCatalogDataSource.saveProductsBatch(firestoreProducts)
            Log.d(tag, "Successfully migrated ${firestoreProducts.size} products to Firestore with canonical category mappings.")

            Result.success(firestoreProducts.size)
        } catch (e: Exception) {
            Log.e(tag, "Catalog migration failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
