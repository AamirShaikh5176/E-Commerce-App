package com.example.e_commerceapp.data.repository

import android.util.Log
import com.example.e_commerceapp.data.data_source.local.AppDao
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import com.example.e_commerceapp.domain.repository.LocalRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Singleton
class LocalRepositoryImpl @Inject constructor(
    private val appDao: AppDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : LocalRepository {

    private fun getActiveUserId(): String {
        val user = auth.currentUser
        return if (user != null && !user.isAnonymous && user.uid.isNotBlank()) user.uid else "guest"
    }

    private val currentUserIdFlow: Flow<String> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val currentId = if (user != null && !user.isAnonymous && user.uid.isNotBlank()) user.uid else "guest"
            trySend(currentId)
        }
        auth.addAuthStateListener(listener)
        trySend(getActiveUserId())
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    // ---------------------- CART OPERATIONS ----------------------

    override suspend fun insertUserCartToDb(userCartEntity: UserCartEntity) {
        val userId = getActiveUserId()
        val entity = userCartEntity.copy(userId = userId)
        appDao.insertUserCart(entity)

        if (userId != "guest") {
            syncCartItemToFirestore(userId, entity)
        }
    }

    override suspend fun getAllCartsFromDb(): Flow<List<UserCartEntity>> {
        return currentUserIdFlow.flatMapLatest { userId ->
            if (userId != "guest") {
                try {
                    appDao.migrateGuestCart(userId)
                } catch (e: Exception) {
                    Log.w("LocalRepo", "Cart migration notice: ${e.message}")
                }
                syncCartFromFirestore(userId)
            }
            appDao.getAllCarts(userId)
        }
    }

    override suspend fun getOneCartByProductId(productId: String): UserCartEntity? {
        val userId = getActiveUserId()
        return appDao.getCartByProductId(userId, productId)
    }

    override suspend fun getTotalPriceFromDb(): Flow<Double> {
        return currentUserIdFlow.flatMapLatest { userId ->
            appDao.getTotalPrice(userId).map { it ?: 0.0 }
        }
    }

    override suspend fun deleteUserCart(userCartEntity: UserCartEntity) {
        val userId = if (userCartEntity.userId.isNotBlank()) userCartEntity.userId else getActiveUserId()
        val entity = userCartEntity.copy(userId = userId)
        appDao.deleteUserCartItem(entity)

        if (userId != "guest") {
            deleteCartItemFromFirestore(userId, entity.productId)
        }
    }

    override suspend fun updateUserCart(userCartEntity: UserCartEntity) {
        val userId = if (userCartEntity.userId.isNotBlank()) userCartEntity.userId else getActiveUserId()
        val entity = userCartEntity.copy(userId = userId)
        appDao.updateUserCartItem(entity)

        if (userId != "guest") {
            syncCartItemToFirestore(userId, entity)
        }
    }

    override suspend fun getBadgeCountFromDb(): Flow<Int> {
        return currentUserIdFlow.flatMapLatest { userId ->
            appDao.getBadgeCount(userId)
        }
    }

    override suspend fun clearCart() {
        val userId = getActiveUserId()
        appDao.clearCartByUserId(userId)
        if (userId != "guest") {
            clearFirestoreCart(userId)
        }
    }

    // ------------------- FAVORITE (WISHLIST) OPERATIONS -------------------

    override suspend fun getAllFavoriteProductsFromDb(): Flow<List<FavoriteProductEntity>> {
        return currentUserIdFlow.flatMapLatest { userId ->
            if (userId != "guest") {
                syncWishlistFromFirestore(userId)
            }
            appDao.getAllFavoriteProducts(userId)
        }
    }

    override suspend fun getOneFavoriteProductByProductId(productId: String): FavoriteProductEntity? {
        val userId = getActiveUserId()
        return appDao.getOneFavoriteProductByProductId(userId, productId)
    }

    override suspend fun insertFavoriteProductToDb(favoriteProductEntity: FavoriteProductEntity) {
        val userId = getActiveUserId()
        val entity = favoriteProductEntity.copy(userId = userId)
        appDao.insertFavoriteItem(entity)

        if (userId != "guest") {
            syncWishlistItemToFirestore(userId, entity)
        }
    }

    override suspend fun deleteFavoriteProduct(favoriteProductEntity: FavoriteProductEntity) {
        val userId = if (favoriteProductEntity.userId.isNotBlank()) favoriteProductEntity.userId else getActiveUserId()
        val entity = favoriteProductEntity.copy(userId = userId)
        appDao.deleteFavoriteItem(entity)

        if (userId != "guest") {
            deleteWishlistItemFromFirestore(userId, entity.productId)
        }
    }

    override suspend fun clearGuestSession() {
        appDao.clearCartByUserId("guest")
        appDao.clearFavoriteByUserId("guest")
    }

    // ------------------- FIRESTORE SYNC HELPERS -------------------

    private fun syncCartFromFirestore(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(3000L) {
                    val snapshot = firestore.collection("users")
                        .document(userId)
                        .collection("cart")
                        .get()
                        .await()

                    val remoteItems = snapshot.documents.mapNotNull { doc ->
                        val productId = doc.getString("productId") ?: doc.id
                        UserCartEntity(
                            userId = userId,
                            productId = productId,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            rating = doc.getString("rating") ?: "0.0",
                            price = doc.getDouble("price") ?: 0.0,
                            availableQuantity = doc.getLong("availableQuantity")?.toInt() ?: 0,
                            quantity = doc.getLong("quantity")?.toInt() ?: 1,
                            image = doc.getString("image") ?: "",
                            categoryId = doc.getString("categoryId") ?: "",
                            categoryName = doc.getString("categoryName") ?: ""
                        )
                    }

                    for (item in remoteItems) {
                        appDao.insertUserCart(item)
                    }
                }
            } catch (e: Exception) {
                Log.w("LocalRepo", "Firestore cart sync warning: ${e.message}")
            }
        }
    }

    private fun syncWishlistFromFirestore(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(3000L) {
                    val snapshot = firestore.collection("users")
                        .document(userId)
                        .collection("wishlist")
                        .get()
                        .await()

                    val remoteItems = snapshot.documents.mapNotNull { doc ->
                        val productId = doc.getString("productId") ?: doc.id
                        FavoriteProductEntity(
                            userId = userId,
                            productId = productId,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            rating = doc.getDouble("rating") ?: 0.0,
                            availableQuantity = doc.getLong("availableQuantity")?.toInt() ?: 0,
                            image = doc.getString("image") ?: "",
                            discountPercentage = doc.getDouble("discountPercentage") ?: 0.0
                        )
                    }

                    for (item in remoteItems) {
                        appDao.insertFavoriteItem(item)
                    }
                }
            } catch (e: Exception) {
                Log.w("LocalRepo", "Firestore wishlist sync warning: ${e.message}")
            }
        }
    }

    private fun syncCartItemToFirestore(userId: String, entity: UserCartEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(2500L) {
                    val data = hashMapOf(
                        "productId" to entity.productId,
                        "title" to entity.title,
                        "description" to entity.description,
                        "rating" to entity.rating,
                        "price" to entity.price,
                        "availableQuantity" to entity.availableQuantity,
                        "quantity" to entity.quantity,
                        "image" to entity.image,
                        "categoryId" to entity.categoryId,
                        "categoryName" to entity.categoryName,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(entity.productId)
                        .set(data)
                        .await()
                }
            } catch (e: Exception) {
                Log.w("LocalRepo", "Firestore sync cart item error: ${e.message}")
            }
        }
    }

    private fun deleteCartItemFromFirestore(userId: String, productId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(2500L) {
                    firestore.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(productId)
                        .delete()
                        .await()
                }
            } catch (e: Exception) {
                Log.w("LocalRepo", "Firestore delete cart item error: ${e.message}")
            }
        }
    }

    private fun clearFirestoreCart(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(3000L) {
                    val snapshot = firestore.collection("users")
                        .document(userId)
                        .collection("cart")
                        .get()
                        .await()
                    for (doc in snapshot.documents) {
                        doc.reference.delete().await()
                    }
                }
            } catch (e: Exception) {
                Log.w("LocalRepo", "Firestore clear cart error: ${e.message}")
            }
        }
    }

    private fun syncWishlistItemToFirestore(userId: String, entity: FavoriteProductEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(2500L) {
                    val data = hashMapOf(
                        "productId" to entity.productId,
                        "title" to entity.title,
                        "description" to entity.description,
                        "price" to entity.price,
                        "rating" to entity.rating,
                        "availableQuantity" to entity.availableQuantity,
                        "image" to entity.image,
                        "discountPercentage" to entity.discountPercentage,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users")
                        .document(userId)
                        .collection("wishlist")
                        .document(entity.productId)
                        .set(data)
                        .await()
                }
            } catch (e: Exception) {
                Log.w("LocalRepo", "Firestore sync wishlist item error: ${e.message}")
            }
        }
    }

    private fun deleteWishlistItemFromFirestore(userId: String, productId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(2500L) {
                    firestore.collection("users")
                        .document(userId)
                        .collection("wishlist")
                        .document(productId)
                        .delete()
                        .await()
                }
            } catch (e: Exception) {
                Log.w("LocalRepo", "Firestore delete wishlist item error: ${e.message}")
            }
        }
    }
}