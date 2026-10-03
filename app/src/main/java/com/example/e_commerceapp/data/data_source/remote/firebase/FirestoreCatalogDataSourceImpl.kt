package com.example.e_commerceapp.data.data_source.remote.firebase

import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.data.dto.firestore.FirestoreCategoryDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreProductDto
import com.example.e_commerceapp.di.coroutine.IoDispatcher
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreCatalogDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val registeredUsersLocalStore: com.example.e_commerceapp.data.data_source.local.RegisteredUsersLocalStore,
) : FirestoreCatalogDataSource {

    private val productsCollection = firestore.collection("products")
    private val categoriesCollection = firestore.collection("categories")
    private val ordersCollection = firestore.collection("orders")
    private val usersCollection = firestore.collection("users")

    override fun getProductsFlow(): Flow<List<ProductEntity>> = callbackFlow {
        val subscription = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(com.example.e_commerceapp.data.demo.DemoCatalogData.toProductEntityList())
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val firestoreProducts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreProductDto::class.java)?.toProductEntity(doc.id)
                }
                trySend(firestoreProducts)
            } else {
                trySend(com.example.e_commerceapp.data.demo.DemoCatalogData.toProductEntityList())
            }
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    override fun getRawProductsFlow(): Flow<List<FirestoreProductDto>> = callbackFlow {
        val subscription = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(com.example.e_commerceapp.data.demo.DemoCatalogData.demoProducts.toList())
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val firestoreProducts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreProductDto::class.java)?.copy(
                        id = doc.getString("id")?.ifBlank { doc.id } ?: doc.id
                    )
                }
                trySend(firestoreProducts)
            } else {
                trySend(com.example.e_commerceapp.data.demo.DemoCatalogData.demoProducts.toList())
            }
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    override fun getProductByIdFlow(productId: String): Flow<ProductDetailsEntity?> = callbackFlow {
        val subscription = productsCollection.document(productId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(com.example.e_commerceapp.data.demo.DemoCatalogData.findProductDetailsById(productId))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val product = snapshot.toObject(FirestoreProductDto::class.java)?.toProductDetailsEntity(snapshot.id)
                trySend(product)
            } else {
                trySend(com.example.e_commerceapp.data.demo.DemoCatalogData.findProductDetailsById(productId))
            }
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    override fun getRawProductByIdFlow(productId: String): Flow<FirestoreProductDto?> = callbackFlow {
        val subscription = productsCollection.document(productId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("CatalogDataSource", "getRawProductByIdFlow error: ${error.message}")
                trySend(com.example.e_commerceapp.data.demo.DemoCatalogData.demoProducts.find { p -> p.id == productId })
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val product = snapshot.toObject(FirestoreProductDto::class.java)?.copy(id = snapshot.id)
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
                android.util.Log.e("CatalogDataSource", "getProductsByCategoryFlow error: ${error.message}")
                val fallback = com.example.e_commerceapp.data.demo.DemoCatalogData.toProductEntityList().filter { prod ->
                    matchesCategoryFilter(prod.category, prod.category, emptyList(), categoryName.trim().lowercase())
                }
                trySend(fallback)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val normalizedCategory = categoryName.trim().lowercase()
                val products = snapshot.documents.mapNotNull { doc ->
                    val dto = doc.toObject(FirestoreProductDto::class.java)
                    if (dto != null && matchesCategoryFilter(dto.categoryId, dto.categoryName, dto.tags, normalizedCategory)) {
                        dto.toProductEntity(doc.id)
                    } else {
                        null
                    }
                }
                trySend(products)
            } else {
                val fallback = com.example.e_commerceapp.data.demo.DemoCatalogData.toProductEntityList().filter { prod ->
                    matchesCategoryFilter(prod.category, prod.category, emptyList(), categoryName.trim().lowercase())
                }
                trySend(fallback)
            }
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    private fun matchesCategoryFilter(catId: String, catName: String, tags: List<String>, target: String): Boolean {
        val id = catId.trim().lowercase()
        val name = catName.trim().lowercase()
        val t = target.trim().lowercase()
        val lowerTags = tags.map { it.trim().lowercase() }

        if (id == t || name == t || lowerTags.contains(t) || name.contains(t) || t.contains(name)) return true
        if (t in listOf("mobiles", "smartphones", "phone", "phones") && (id in listOf("mobiles", "smartphones") || name.contains("mobile") || lowerTags.contains("mobiles") || lowerTags.contains("smartphones"))) return true
        if (t in listOf("electronics", "electronic", "laptops", "tablets") && (id in listOf("electronics", "laptops", "tablets") || name.contains("electronic") || lowerTags.contains("electronics"))) return true
        if (t in listOf("fashion", "clothing", "shoes", "wear") && (id == "fashion" || name.contains("fashion") || lowerTags.contains("fashion") || lowerTags.contains("shoes"))) return true
        if (t in listOf("beauty", "skincare", "grooming", "cosmetics") && (id in listOf("beauty", "skincare", "fragrances") || name.contains("beauty") || lowerTags.contains("beauty"))) return true
        if (t in listOf("home", "furniture", "kitchen", "appliances") && (id in listOf("home", "furniture") || name.contains("home") || lowerTags.contains("home"))) return true
        if (t in listOf("groceries", "grocery", "food") && (id in listOf("groceries", "grocery") || name.contains("grocer") || lowerTags.contains("groceries"))) return true
        if (t in listOf("sports", "fitness", "sport") && (id in listOf("sports", "fitness") || name.contains("sport") || lowerTags.contains("sports"))) return true
        if (t in listOf("accessories", "accessory", "watches") && (id == "accessories" || name.contains("accessor") || lowerTags.contains("accessories") || lowerTags.contains("watch"))) return true
        return false
    }

    override fun searchProductsFlow(query: String): Flow<List<SearchProductEntity>> =
        getProductsFlow().map { products ->
            val trimmed = query.trim().lowercase()
            if (trimmed.isBlank()) {
                emptyList()
            } else {
                products.filter { product ->
                    val cat = product.category.lowercase()
                    val title = product.title.lowercase()
                    val desc = product.description.lowercase()
                    val brand = product.brand.lowercase()

                    title.contains(trimmed) ||
                    desc.contains(trimmed) ||
                    brand.contains(trimmed) ||
                    cat.contains(trimmed) ||
                    trimmed.contains(cat) ||
                    (trimmed in listOf("mobiles", "smartphones", "phone", "phones") && (cat == "mobiles" || cat == "smartphones" || cat.contains("mobile") || cat.contains("phone"))) ||
                    (trimmed in listOf("electronics", "electronic", "laptops", "tablets") && (cat == "electronics" || cat == "laptops" || cat == "tablets" || cat.contains("electronic"))) ||
                    (trimmed in listOf("fashion", "clothing", "shoes", "wear") && (cat == "fashion" || cat.contains("shirt") || cat.contains("shoes") || cat.contains("dress") || cat.contains("jeans"))) ||
                    (trimmed in listOf("beauty", "skincare", "grooming", "cosmetics") && (cat == "beauty" || cat.contains("serum") || cat.contains("grooming") || cat.contains("skin"))) ||
                    (trimmed in listOf("home", "furniture", "kitchen", "appliances") && (cat == "home" || cat.contains("mattress") || cat.contains("bedsheet") || cat.contains("appliance"))) ||
                    (trimmed in listOf("groceries", "grocery", "food") && (cat == "groceries" || cat.contains("tea") || cat.contains("oil") || cat.contains("food"))) ||
                    (trimmed in listOf("sports", "fitness", "sport") && (cat == "sports" || cat.contains("racket") || cat.contains("football") || cat.contains("yoga"))) ||
                    (trimmed in listOf("accessories", "accessory", "watches", "watch") && (cat == "accessories" || cat.contains("watch") || cat.contains("sunglasses") || cat.contains("bag")))
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

    override fun getRawCategoriesFlow(): Flow<List<FirestoreCategoryDto>> = callbackFlow {
        val subscription = categoriesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val categories = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreCategoryDto::class.java)?.copy(id = doc.id)
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
        com.example.e_commerceapp.data.demo.DemoCatalogData.updateProduct(productWithId)
        docRef.set(productWithId).awaitTask()
    }

    override suspend fun deleteProduct(productId: String): Unit = withContext(ioDispatcher) {
        productsCollection.document(productId).delete().awaitTask()
    }

    override suspend fun updateProductStock(
        productId: String,
        inStock: Boolean,
        stockQuantity: Int?
    ): Unit = withContext(ioDispatcher) {
        val updates = mutableMapOf<String, Any>("inStock" to inStock)
        if (stockQuantity != null) {
            updates["stockQuantity"] = stockQuantity
        }
        productsCollection.document(productId).update(updates).awaitTask()
    }

    override suspend fun saveCategory(category: FirestoreCategoryDto): Unit = withContext(ioDispatcher) {
        val docRef = if (category.slug.isNotBlank()) {
            categoriesCollection.document(category.slug)
        } else if (category.id.isNotBlank()) {
            categoriesCollection.document(category.id)
        } else {
            categoriesCollection.document()
        }
        val toSave = if (category.id.isBlank()) category.copy(id = docRef.id) else category
        docRef.set(toSave).awaitTask()
    }

    override suspend fun deleteCategory(categoryId: String): Unit = withContext(ioDispatcher) {
        categoriesCollection.document(categoryId).delete().awaitTask()
    }

    override suspend fun saveProductsBatch(products: List<FirestoreProductDto>): Unit = withContext(ioDispatcher) {
        // Query existing products so we NEVER overwrite existing products or user edits in Firestore
        val existingDocIds = try {
            productsCollection.get().awaitTask().documents.map { it.id }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
        val newProducts = products.filter { it.id.isBlank() || it.id !in existingDocIds }
        if (newProducts.isEmpty()) {
            android.util.Log.d("CatalogDataSource", "All products already exist in Firestore. Skipping batch write to preserve custom changes.")
            return@withContext
        }
        newProducts.chunked(400).forEach { chunk ->
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
        val existingDocIds = try {
            categoriesCollection.get().awaitTask().documents.map { it.id }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
        val newCategories = categories.filter { category ->
            val docId = if (category.slug.isNotBlank()) {
                category.slug
            } else if (category.id.isNotBlank()) {
                category.id
            } else {
                ""
            }
            docId.isBlank() || docId !in existingDocIds
        }
        if (newCategories.isEmpty()) {
            return@withContext
        }
        newCategories.chunked(400).forEach { chunk ->
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
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun hasCategories(): Boolean = withContext(ioDispatcher) {
        try {
            val snapshot = categoriesCollection.limit(1).get().awaitTask()
            !snapshot.isEmpty
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getProductsCount(): Int = withContext(ioDispatcher) {
        try {
            val snapshot = productsCollection.get().awaitTask()
            snapshot.size()
        } catch (_: Exception) {
            -1
        }
    }

    override fun getOrdersFlow(): Flow<List<FirestoreOrderDto>> = callbackFlow {
        trySend(emptyList())
        val subscription = ordersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("CatalogDataSource", "getOrdersFlow error: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val orders = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreOrderDto::class.java)?.copy(orderId = doc.id)
                }.sortedByDescending { it.createdAt }
                trySend(orders)
            }
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    override fun getUserOrdersFlow(userId: String): Flow<List<FirestoreOrderDto>> = callbackFlow {
        trySend(emptyList())
        val subscription = ordersCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("CatalogDataSource", "getUserOrdersFlow error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FirestoreOrderDto::class.java)?.copy(orderId = doc.id)
                    }.sortedByDescending { it.createdAt }
                    trySend(orders)
                }
            }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    override fun getOrderByIdFlow(orderId: String): Flow<FirestoreOrderDto?> = callbackFlow {
        val subscription = ordersCollection.document(orderId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("CatalogDataSource", "getOrderByIdFlow error: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }
            val order = snapshot?.toObject(FirestoreOrderDto::class.java)?.copy(orderId = snapshot.id)
            trySend(order)
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

    override suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        trackingNumber: String,
        courierPartner: String
    ): Unit = withContext(ioDispatcher) {
        val updates = mutableMapOf<String, Any>(
            "orderStatus" to status,
            "updatedAt" to System.currentTimeMillis()
        )
        if (trackingNumber.isNotBlank()) updates["trackingNumber"] = trackingNumber
        if (courierPartner.isNotBlank()) updates["courierPartner"] = courierPartner
        ordersCollection.document(orderId).update(updates).awaitTask()
    }

    override suspend fun updateOrderStatusAndPayment(
        orderId: String,
        status: String,
        paymentStatus: String?,
        trackingNumber: String,
        courierPartner: String
    ): Unit = withContext(ioDispatcher) {
        val updates = mutableMapOf<String, Any>(
            "orderStatus" to status,
            "updatedAt" to System.currentTimeMillis()
        )
        if (!paymentStatus.isNullOrBlank()) updates["paymentStatus"] = paymentStatus
        if (trackingNumber.isNotBlank()) updates["trackingNumber"] = trackingNumber
        if (courierPartner.isNotBlank()) updates["courierPartner"] = courierPartner
        ordersCollection.document(orderId).update(updates).awaitTask()
    }

    override suspend fun requestOrderCancellation(
        orderId: String,
        reason: String
    ): Unit = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val updates = mapOf<String, Any>(
            "orderStatus" to "CANCELLATION_REQUESTED",
            "cancellationRequested" to true,
            "cancelReason" to reason,
            "cancellationRequestedAt" to now,
            "updatedAt" to now
        )
        ordersCollection.document(orderId).update(updates).awaitTask()
    }

    override suspend fun requestOrderReturn(
        orderId: String,
        reason: String
    ): Unit = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val updates = mapOf<String, Any>(
            "orderStatus" to "RETURN_REQUESTED",
            "returnRequested" to true,
            "returnReason" to reason,
            "returnRequestedAt" to now,
            "pickupRequired" to true,
            "pickupStatus" to "PENDING",
            "updatedAt" to now
        )
        ordersCollection.document(orderId).update(updates).awaitTask()
    }

    override suspend fun updateOrderAdminAction(
        orderId: String,
        updates: Map<String, Any>,
        auditEntry: Map<String, Any>
    ): Unit = withContext(ioDispatcher) {
        val finalUpdates = updates.toMutableMap()
        finalUpdates["updatedAt"] = System.currentTimeMillis()
        finalUpdates["adminAuditTrail"] = FieldValue.arrayUnion(auditEntry)
        ordersCollection.document(orderId).update(finalUpdates).awaitTask()
    }

    override fun getUsersFlow(): Flow<List<UserInformationEntity>> = callbackFlow {
        trySend(registeredUsersLocalStore.getAllUsers())

        usersCollection.get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                val firestoreUsers = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id")?.ifBlank { doc.id } ?: doc.id
                    val name = doc.getString("name")?.ifBlank { null }
                        ?: doc.getString("email")?.substringBefore('@')?.replaceFirstChar { it.uppercase() }
                        ?: "Customer"
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val image = doc.getString("image") ?: ""
                    val role = doc.getString("role") ?: "customer"

                    UserInformationEntity(
                        id = id,
                        name = name,
                        email = email,
                        phone = phone,
                        image = image,
                        password = "",
                        token = "",
                        role = role,
                    )
                }
                val merged = (registeredUsersLocalStore.getAllUsers() + firestoreUsers)
                    .distinctBy { it.email.lowercase() }
                trySend(merged)
            }
        }.addOnFailureListener { e ->
            android.util.Log.e("CatalogDataSource", "Initial usersCollection.get failed", e)
        }

        val subscription = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("CatalogDataSource", "getUsersFlow snapshot error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val firestoreUsers = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id")?.ifBlank { doc.id } ?: doc.id
                    val name = doc.getString("name")?.ifBlank { null }
                        ?: doc.getString("email")?.substringBefore('@')?.replaceFirstChar { it.uppercase() }
                        ?: "Customer"
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val image = doc.getString("image") ?: ""
                    val role = doc.getString("role") ?: "customer"

                    UserInformationEntity(
                        id = id,
                        name = name,
                        email = email,
                        phone = phone,
                        image = image,
                        password = "",
                        token = "",
                        role = role,
                    )
                }
                val merged = (registeredUsersLocalStore.getAllUsers() + firestoreUsers)
                    .distinctBy { it.email.lowercase() }
                trySend(merged)
            }
        }
        awaitClose { subscription.remove() }
    }.flowOn(ioDispatcher)

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
