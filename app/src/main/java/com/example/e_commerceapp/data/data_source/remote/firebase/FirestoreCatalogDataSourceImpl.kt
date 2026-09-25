package com.example.e_commerceapp.data.data_source.remote.firebase

import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.data.dto.firestore.FirestoreCategoryDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreProductDto
import com.example.e_commerceapp.di.coroutine.IoDispatcher
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreCatalogDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FirestoreCatalogDataSource {

    private val productsCollection = firestore.collection("products")
    private val categoriesCollection = firestore.collection("categories")

    override fun getProductsFlow(): Flow<List<ProductEntity>> = callbackFlow {
        val subscription = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val products = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreProductDto::class.java)?.toProductEntity(doc.id)
                }
                trySend(products)
            }
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    override fun getProductByIdFlow(productId: String): Flow<ProductDetailsEntity?> = callbackFlow {
        val subscription = productsCollection.document(productId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val product = snapshot.toObject(FirestoreProductDto::class.java)?.toProductDetailsEntity(snapshot.id)
                trySend(product)
            } else {
                trySend(null)
            }
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    override fun getProductsByCategoryFlow(categoryName: String): Flow<List<ProductEntity>> = callbackFlow {
        val subscription = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val normalizedCategory = categoryName.trim().lowercase()
                val products = snapshot.documents.mapNotNull { doc ->
                    val dto = doc.toObject(FirestoreProductDto::class.java)
                    if (dto != null && (
                            dto.categoryId.lowercase() == normalizedCategory ||
                            dto.categoryName.lowercase() == normalizedCategory ||
                            dto.tags.any { it.lowercase() == normalizedCategory }
                        )) {
                        dto.toProductEntity(doc.id)
                    } else {
                        null
                    }
                }
                trySend(products)
            }
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    override fun searchProductsFlow(query: String): Flow<List<SearchProductEntity>> =
        getProductsFlow().map { products ->
            val trimmed = query.trim().lowercase()
            if (trimmed.isBlank()) {
                emptyList()
            } else {
                products.filter {
                    it.title.lowercase().contains(trimmed) ||
                    it.description.lowercase().contains(trimmed) ||
                    it.brand.lowercase().contains(trimmed)
                }.map {
                    SearchProductEntity(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        price = it.price,
                        imageUrl = it.imageUrl,
                        rating = it.rating,
                        quantity = it.quantity,
                        discountPercentage = it.discountPercentage,
                        isFavorite = false,
                    )
                }
            }
        }.flowOn(ioDispatcher)

    override fun getCategoriesFlow(): Flow<List<Category>> = callbackFlow {
        val subscription = categoriesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val categories = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreCategoryDto::class.java)?.toCategory()
                }
                trySend(categories)
            }
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    override suspend fun saveProduct(product: FirestoreProductDto): Unit = withContext(ioDispatcher) {
        val docRef = if (product.id.isNotBlank()) {
            productsCollection.document(product.id)
        } else {
            productsCollection.document()
        }
        val productWithId = if (product.id.isBlank()) product.copy(id = docRef.id) else product
        docRef.set(productWithId).awaitTask()
    }

    override suspend fun saveProductsBatch(products: List<FirestoreProductDto>): Unit = withContext(ioDispatcher) {
        // Chunk to stay well within Firestore's 500-operation batch limit
        products.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            for (product in chunk) {
                val docRef = if (product.id.isNotBlank()) {
                    productsCollection.document(product.id)
                } else {
                    productsCollection.document()
                }
                val toSave = if (product.id.isBlank()) product.copy(id = docRef.id) else product
                batch.set(docRef, toSave)
            }
            batch.commit().awaitTask()
        }
    }

    override suspend fun saveCategoriesBatch(categories: List<FirestoreCategoryDto>): Unit = withContext(ioDispatcher) {
        categories.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            for (category in chunk) {
                val docRef = if (category.slug.isNotBlank()) {
                    categoriesCollection.document(category.slug)
                } else if (category.id.isNotBlank()) {
                    categoriesCollection.document(category.id)
                } else {
                    categoriesCollection.document()
                }
                val toSave = if (category.id.isBlank()) category.copy(id = docRef.id) else category
                batch.set(docRef, toSave)
            }
            batch.commit().awaitTask()
        }
    }

    override suspend fun hasProducts(): Boolean = withContext(ioDispatcher) {
        try {
            val snapshot = productsCollection.limit(1).get().awaitTask()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}
