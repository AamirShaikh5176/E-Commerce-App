package com.example.e_commerceapp.data.repository

import com.example.e_commerceapp.data.model.supercoins.SuperCoinsTransaction
import com.example.e_commerceapp.data.model.supercoins.SuperCoinsWallet
import com.example.e_commerceapp.domain.repository.SuperCoinsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

class SuperCoinsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @com.example.e_commerceapp.di.coroutine.IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SuperCoinsRepository {

    private fun getWalletDocRef(userId: String) =
        firestore.collection("users").document(userId).collection("wallet").document("supercoins")

    private fun getTransactionsCollectionRef(userId: String) =
        getWalletDocRef(userId).collection("transactions")

    override fun getWalletFlow(userId: String): Flow<SuperCoinsWallet> = callbackFlow {
        if (userId.isBlank()) {
            trySend(SuperCoinsWallet())
            close()
            return@callbackFlow
        }

        val listener = getWalletDocRef(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(SuperCoinsWallet())
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val wallet = SuperCoinsWallet(
                    balance = snapshot.getLong("balance") ?: 0L,
                    lifetimeEarned = snapshot.getLong("lifetimeEarned") ?: 0L,
                    lifetimeRedeemed = snapshot.getLong("lifetimeRedeemed") ?: 0L,
                    createdAt = snapshot.getLong("createdAt") ?: 0L,
                    updatedAt = snapshot.getLong("updatedAt") ?: 0L
                )
                trySend(wallet)
            } else {
                trySend(SuperCoinsWallet())
            }
        }

        awaitClose { listener.remove() }
    }.flowOn(ioDispatcher)

    override suspend fun getWallet(userId: String): Result<SuperCoinsWallet> = withContext(ioDispatcher) {
        runCatching {
            if (userId.isBlank()) return@runCatching SuperCoinsWallet()
            val snap = getWalletDocRef(userId).get().await()
            if (snap != null && snap.exists()) {
                SuperCoinsWallet(
                    balance = snap.getLong("balance") ?: 0L,
                    lifetimeEarned = snap.getLong("lifetimeEarned") ?: 0L,
                    lifetimeRedeemed = snap.getLong("lifetimeRedeemed") ?: 0L,
                    createdAt = snap.getLong("createdAt") ?: 0L,
                    updatedAt = snap.getLong("updatedAt") ?: 0L
                )
            } else {
                SuperCoinsWallet()
            }
        }
    }

    override fun getTransactionsFlow(userId: String): Flow<List<SuperCoinsTransaction>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = getTransactionsCollectionRef(userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    SuperCoinsTransaction(
                        id = doc.getString("id") ?: doc.id,
                        type = doc.getString("type").orEmpty(),
                        amount = doc.getLong("amount") ?: 0L,
                        description = doc.getString("description").orEmpty(),
                        orderId = doc.getString("orderId").orEmpty(),
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } ?: emptyList()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }.flowOn(ioDispatcher)

    override suspend fun initializeWelcomeBonusIfEligible(userId: String): Result<Boolean> = withContext(ioDispatcher) {
        runCatching {
            if (userId.isBlank()) return@runCatching false

            val walletDocRef = getWalletDocRef(userId)
            val txnCollectionRef = getTransactionsCollectionRef(userId)

            var granted = false

            firestore.runTransaction { transaction ->
                val walletSnap = transaction.get(walletDocRef)
                if (!walletSnap.exists()) {
                    val now = System.currentTimeMillis()
                    val bonusAmount = 500L

                    val walletData = hashMapOf<String, Any>(
                        "balance" to bonusAmount,
                        "lifetimeEarned" to bonusAmount,
                        "lifetimeRedeemed" to 0L,
                        "createdAt" to now,
                        "updatedAt" to now
                    )
                    transaction.set(walletDocRef, walletData)

                    val newTxnRef = txnCollectionRef.document()
                    val txnData = hashMapOf<String, Any>(
                        "id" to newTxnRef.id,
                        "type" to "WELCOME_BONUS",
                        "amount" to bonusAmount,
                        "description" to "Welcome Bonus",
                        "orderId" to "",
                        "timestamp" to now
                    )
                    transaction.set(newTxnRef, txnData)
                    granted = true
                }
            }.await()

            granted
        }
    }

    override suspend fun redeemSuperCoinsForOrder(
        userId: String,
        orderId: String,
        amountToRedeem: Long
    ): Result<Long> = withContext(ioDispatcher) {
        runCatching {
            if (userId.isBlank() || amountToRedeem <= 0L) return@runCatching 0L

            val walletDocRef = getWalletDocRef(userId)
            val txnCollectionRef = getTransactionsCollectionRef(userId)

            firestore.runTransaction { transaction ->
                val walletSnap = transaction.get(walletDocRef)
                val currentBalance = if (walletSnap.exists()) {
                    walletSnap.getLong("balance") ?: 0L
                } else {
                    0L
                }
                val currentLifetimeRedeemed = if (walletSnap.exists()) {
                    walletSnap.getLong("lifetimeRedeemed") ?: 0L
                } else {
                    0L
                }

                if (currentBalance < amountToRedeem) {
                    throw IllegalStateException("Insufficient SuperCoins balance: Available $currentBalance, Requested $amountToRedeem")
                }

                val now = System.currentTimeMillis()
                val newBalance = currentBalance - amountToRedeem
                val newLifetimeRedeemed = currentLifetimeRedeemed + amountToRedeem

                val updates = hashMapOf<String, Any>(
                    "balance" to newBalance,
                    "lifetimeRedeemed" to newLifetimeRedeemed,
                    "updatedAt" to now
                )
                transaction.update(walletDocRef, updates)

                val newTxnRef = txnCollectionRef.document()
                val txnData = hashMapOf<String, Any>(
                    "id" to newTxnRef.id,
                    "type" to "ORDER_REDEMPTION",
                    "amount" to -amountToRedeem,
                    "description" to "Redeemed on Order #${orderId.takeLast(8).uppercase()}",
                    "orderId" to orderId,
                    "timestamp" to now
                )
                transaction.set(newTxnRef, txnData)
            }.await()

            amountToRedeem
        }
    }

    override suspend fun awardOrderRewardIfEligible(
        orderId: String,
        userId: String
    ): Result<Long> = withContext(ioDispatcher) {
        runCatching {
            if (orderId.isBlank() || userId.isBlank()) return@runCatching 0L

            val orderRef = firestore.collection("orders").document(orderId)
            val walletDocRef = getWalletDocRef(userId)
            val txnCollectionRef = getTransactionsCollectionRef(userId)

            var awardedCoins = 0L

            firestore.runTransaction { transaction ->
                val orderSnap = transaction.get(orderRef)
                if (!orderSnap.exists()) return@runTransaction

                val isAlreadyGranted = orderSnap.getBoolean("superCoinsRewardGranted") ?: false
                if (isAlreadyGranted) return@runTransaction // Idempotency check: never double-award!

                val orderStatus = orderSnap.getString("orderStatus").orEmpty()
                val paymentStatus = orderSnap.getString("paymentStatus").orEmpty()

                if (orderStatus.equals("CANCELLED", ignoreCase = true)) {
                    // Cancelled orders cannot earn rewards
                    return@runTransaction
                }

                // Check eligibility: Order must be CONFIRMED/DELIVERED or payment PAID/COMPLETED
                val isEligible = orderStatus.equals("CONFIRMED", ignoreCase = true) ||
                        orderStatus.equals("DELIVERED", ignoreCase = true) ||
                        orderStatus.equals("SHIPPED", ignoreCase = true) ||
                        orderStatus.equals("PROCESSING", ignoreCase = true) ||
                        paymentStatus.equals("PAID", ignoreCase = true) ||
                        paymentStatus.equals("COMPLETED", ignoreCase = true)

                if (!isEligible) {
                    return@runTransaction
                }

                // Authoritative total amount
                val itemsTotal = orderSnap.getDouble("itemsTotal") ?: 0.0
                val finalTotal = orderSnap.getDouble("finalTotal") ?: itemsTotal
                val baseAmountForReward = if (itemsTotal > 0.0) itemsTotal else finalTotal

                // 10% rounded down to nearest whole integer
                val calculatedCoins = (baseAmountForReward * 0.10).toLong()

                if (calculatedCoins <= 0L) {
                    // Mark as granted even if 0 coins to prevent re-evaluating
                    transaction.update(orderRef, "superCoinsRewardGranted", true)
                    return@runTransaction
                }

                val walletSnap = transaction.get(walletDocRef)
                val currentBalance = if (walletSnap.exists()) walletSnap.getLong("balance") ?: 0L else 0L
                val currentLifetimeEarned = if (walletSnap.exists()) walletSnap.getLong("lifetimeEarned") ?: 0L else 0L
                val now = System.currentTimeMillis()

                val newBalance = currentBalance + calculatedCoins
                val newLifetimeEarned = currentLifetimeEarned + calculatedCoins

                if (!walletSnap.exists()) {
                    val walletData = hashMapOf<String, Any>(
                        "balance" to newBalance,
                        "lifetimeEarned" to newLifetimeEarned,
                        "lifetimeRedeemed" to 0L,
                        "createdAt" to now,
                        "updatedAt" to now
                    )
                    transaction.set(walletDocRef, walletData)
                } else {
                    val updates = hashMapOf<String, Any>(
                        "balance" to newBalance,
                        "lifetimeEarned" to newLifetimeEarned,
                        "updatedAt" to now
                    )
                    transaction.update(walletDocRef, updates)
                }

                val newTxnRef = txnCollectionRef.document()
                val txnData = hashMapOf<String, Any>(
                    "id" to newTxnRef.id,
                    "type" to "ORDER_REWARD",
                    "amount" to calculatedCoins,
                    "description" to "10% Reward on Order #${orderId.takeLast(8).uppercase()}",
                    "orderId" to orderId,
                    "timestamp" to now
                )
                transaction.set(newTxnRef, txnData)

                // Mark order reward as granted
                transaction.update(
                    orderRef,
                    mapOf(
                        "superCoinsRewardGranted" to true,
                        "superCoinsEarned" to calculatedCoins
                    )
                )

                awardedCoins = calculatedCoins
            }.await()

            awardedCoins
        }
    }

    override suspend fun refundRedeemedCoinsIfEligible(
        orderId: String,
        userId: String
    ): Result<Long> = withContext(ioDispatcher) {
        runCatching {
            if (orderId.isBlank() || userId.isBlank()) return@runCatching 0L

            val orderRef = firestore.collection("orders").document(orderId)
            val walletDocRef = getWalletDocRef(userId)
            val txnCollectionRef = getTransactionsCollectionRef(userId)

            var refundedAmount = 0L

            firestore.runTransaction { transaction ->
                val orderSnap = transaction.get(orderRef)
                if (!orderSnap.exists()) return@runTransaction

                val isAlreadyRefunded = orderSnap.getBoolean("superCoinsRefunded") ?: false
                if (isAlreadyRefunded) return@runTransaction // Idempotency check: never double refund!

                val coinsRedeemed = orderSnap.getLong("superCoinsRedeemed") ?: 0L
                if (coinsRedeemed <= 0L) {
                    transaction.update(orderRef, "superCoinsRefunded", true)
                    return@runTransaction
                }

                val walletSnap = transaction.get(walletDocRef)
                val currentBalance = if (walletSnap.exists()) walletSnap.getLong("balance") ?: 0L else 0L
                val currentLifetimeRedeemed = if (walletSnap.exists()) walletSnap.getLong("lifetimeRedeemed") ?: 0L else 0L
                val now = System.currentTimeMillis()

                val newBalance = currentBalance + coinsRedeemed
                val newLifetimeRedeemed = (currentLifetimeRedeemed - coinsRedeemed).coerceAtLeast(0L)

                if (!walletSnap.exists()) {
                    val walletData = hashMapOf<String, Any>(
                        "balance" to newBalance,
                        "lifetimeEarned" to newBalance,
                        "lifetimeRedeemed" to 0L,
                        "createdAt" to now,
                        "updatedAt" to now
                    )
                    transaction.set(walletDocRef, walletData)
                } else {
                    val updates = hashMapOf<String, Any>(
                        "balance" to newBalance,
                        "lifetimeRedeemed" to newLifetimeRedeemed,
                        "updatedAt" to now
                    )
                    transaction.update(walletDocRef, updates)
                }

                val newTxnRef = txnCollectionRef.document()
                val txnData = hashMapOf<String, Any>(
                    "id" to newTxnRef.id,
                    "type" to "REFUND",
                    "amount" to coinsRedeemed,
                    "description" to "Refund for cancelled Order #${orderId.takeLast(8).uppercase()}",
                    "orderId" to orderId,
                    "timestamp" to now
                )
                transaction.set(newTxnRef, txnData)

                // Update order flags
                transaction.update(
                    orderRef,
                    mapOf(
                        "superCoinsRefunded" to true,
                        "superCoinsRewardGranted" to true // prevent reward from being granted after cancellation
                    )
                )

                refundedAmount = coinsRedeemed
            }.await()

            refundedAmount
        }
    }
}
