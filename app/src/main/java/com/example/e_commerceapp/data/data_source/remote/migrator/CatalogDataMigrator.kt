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
     * Seeds canonical categories and the 48 curated authoritative products
     * with full details (INR prices, descriptions, images, reviews, stock) into Firestore.
     */
    suspend fun migrateCatalog(force: Boolean = false): Result<Int> = withContext(ioDispatcher) {
        try {
            // 1. Seed canonical categories into /categories
            val categoriesToSeed = DemoCatalogData.demoCategoryDtos
            firestoreCatalogDataSource.saveCategoriesBatch(categoriesToSeed)
            Log.d(tag, "Migrated ${categoriesToSeed.size} canonical categories to Firestore.")

            // 2. Seed authoritative curated catalog (48 high-quality products across all categories)
            val firestoreProducts = DemoCatalogData.demoProducts

            firestoreCatalogDataSource.saveProductsBatch(firestoreProducts)
            Log.d(tag, "Successfully migrated ${firestoreProducts.size} products to Firestore with canonical category mappings.")

            Result.success(firestoreProducts.size)
        } catch (e: Exception) {
            Log.e(tag, "Catalog migration failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
