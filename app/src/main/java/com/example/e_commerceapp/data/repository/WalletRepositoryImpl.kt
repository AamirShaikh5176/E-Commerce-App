package com.example.e_commerceapp.data.repository

import com.example.e_commerceapp.data.data_source.remote.api.WalletAddMoneyRequest
import com.example.e_commerceapp.data.data_source.remote.api.WalletApiService
import com.example.e_commerceapp.data.data_source.remote.api.WalletVerifyCreditRequest
import com.example.e_commerceapp.data.dto.payment.RazorpayOrderResponseDto
import com.example.e_commerceapp.data.model.wallet.WalletBalance
import com.example.e_commerceapp.data.model.wallet.WalletTransaction
import com.example.e_commerceapp.domain.repository.WalletRepository
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

class WalletRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val walletApiService: WalletApiService,
    @com.example.e_commerceapp.di.coroutine.IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : WalletRepository {

    private fun getWalletDocRef(userId: String) =
        firestore.collection("users").document(userId).collection("wallet").document("money")

    private fun getTransactionsCollectionRef(userId: String) =
        getWalletDocRef(userId).collection("transactions")

    override fun getBalanceFlow(userId: String): Flow<WalletBalance> = callbackFlow {
        if (userId.isBlank()) {
            trySend(WalletBalance())
            close()
            return@callbackFlow
        }

        val listener = getWalletDocRef(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(WalletBalance())
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val wallet = WalletBalance(
                    balance = snapshot.getDouble("balance") ?: 0.0,
                    lifetimeAdded = snapshot.getDouble("lifetimeAdded") ?: 0.0,
                    lifetimeSpent = snapshot.getDouble("lifetimeSpent") ?: 0.0,
                    createdAt = snapshot.getLong("createdAt") ?: 0L,
                    updatedAt = snapshot.getLong("updatedAt") ?: 0L,
                )
                trySend(wallet)
            } else {
                trySend(WalletBalance())
            }
        }

        awaitClose { listener.remove() }
    }.flowOn(ioDispatcher)

    override suspend fun getBalance(userId: String): Result<WalletBalance> = withContext(ioDispatcher) {
        runCatching {
            if (userId.isBlank()) return@runCatching WalletBalance()
            val snap = getWalletDocRef(userId).get().await()
            if (snap != null && snap.exists()) {
                WalletBalance(
                    balance = snap.getDouble("balance") ?: 0.0,
                    lifetimeAdded = snap.getDouble("lifetimeAdded") ?: 0.0,
                    lifetimeSpent = snap.getDouble("lifetimeSpent") ?: 0.0,
                    createdAt = snap.getLong("createdAt") ?: 0L,
                    updatedAt = snap.getLong("updatedAt") ?: 0L,
                )
            } else {
                WalletBalance()
            }
        }
    }

    override fun getTransactionsFlow(userId: String): Flow<List<WalletTransaction>> = callbackFlow {
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
                    WalletTransaction(
                        id = doc.getString("id") ?: doc.id,
                        type = doc.getString("type").orEmpty(),
                        amount = doc.getDouble("amount") ?: 0.0,
                        description = doc.getString("description").orEmpty(),
                        orderId = doc.getString("orderId").orEmpty(),
                        razorpayPaymentId = doc.getString("razorpayPaymentId").orEmpty(),
                        timestamp = doc.getLong("timestamp") ?: 0L,
                    )
                } ?: emptyList()
                trySend(list)
            }

        awaitClose { listener.remove() }
    }.flowOn(ioDispatcher)

    override suspend fun createAddMoneyOrder(
        userId: String,
        amount: Double,
    ): Result<RazorpayOrderResponseDto> = withContext(ioDispatcher) {
        runCatching {
            val request = WalletAddMoneyRequest(userId = userId, amount = amount)
            val response = walletApiService.createAddMoneyOrder(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!
            } else {
                val errorMsg = response.body()?.message
                    ?: response.errorBody()?.string()
                    ?: "Failed to create wallet top-up order (${response.code()})"
                throw Exception(errorMsg)
            }
        }
    }

    override suspend fun verifyAndCreditWallet(
        userId: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String,
        amount: Double,
    ): Result<Boolean> = withContext(ioDispatcher) {
        runCatching {
            val request = WalletVerifyCreditRequest(
                userId = userId,
                razorpayOrderId = razorpayOrderId,
                razorpayPaymentId = razorpayPaymentId,
                razorpaySignature = razorpaySignature,
                amount = amount,
            )
            val response = walletApiService.verifyAndCreditWallet(request)
            if (response.isSuccessful && response.body()?.verified == true) {
                true
            } else {
                val errorMsg = response.body()?.message ?: "Wallet credit verification failed"
                throw Exception(errorMsg)
            }
        }
    }

    override suspend fun deductForOrder(
        userId: String,
        orderId: String,
        amount: Double,
    ): Result<Double> = withContext(ioDispatcher) {
        runCatching {
            if (userId.isBlank() || amount <= 0.0) return@runCatching 0.0

            try {
                val apiResp = walletApiService.deductForOrder(
                    com.example.e_commerceapp.data.data_source.remote.api.WalletDeductOrderRequest(
                        userId = userId,
                        orderId = orderId,
                        amount = amount
                    )
                )
                if (apiResp.isSuccessful && apiResp.body()?.success == true) {
                    return@runCatching apiResp.body()!!.deducted
                }
            } catch (e: Exception) {
                // If network/offline, fallback to atomic Firestore transaction below
            }

            val walletDocRef = getWalletDocRef(userId)
            val txnCollectionRef = getTransactionsCollectionRef(userId)

            firestore.runTransaction { transaction ->
                val walletSnap = transaction.get(walletDocRef)
                val currentBalance = if (walletSnap.exists()) {
                    walletSnap.getDouble("balance") ?: 0.0
                } else {
                    0.0
                }
                val currentLifetimeSpent = if (walletSnap.exists()) {
                    walletSnap.getDouble("lifetimeSpent") ?: 0.0
                } else {
                    0.0
                }

                if (currentBalance < amount) {
                    throw IllegalStateException(
                        "Insufficient wallet balance: Available ₹${"%.2f".format(currentBalance)}, Requested ₹${"%.2f".format(amount)}"
                    )
                }

                val now = System.currentTimeMillis()
                val newBalance = currentBalance - amount
                val newLifetimeSpent = currentLifetimeSpent + amount

                val updates = hashMapOf<String, Any>(
                    "balance" to newBalance,
                    "lifetimeSpent" to newLifetimeSpent,
                    "updatedAt" to now,
                )

                if (walletSnap.exists()) {
                    transaction.update(walletDocRef, updates)
                } else {
                    val walletData = hashMapOf<String, Any>(
                        "balance" to newBalance,
                        "lifetimeAdded" to 0.0,
                        "lifetimeSpent" to newLifetimeSpent,
                        "createdAt" to now,
                        "updatedAt" to now,
                    )
                    transaction.set(walletDocRef, walletData)
                }

                val newTxnRef = txnCollectionRef.document()
                val txnData = hashMapOf<String, Any>(
                    "id" to newTxnRef.id,
                    "type" to "ORDER_PAYMENT",
                    "amount" to -amount,
                    "description" to "Payment for Order #${orderId.takeLast(8).uppercase()}",
                    "orderId" to orderId,
                    "razorpayPaymentId" to "",
                    "timestamp" to now,
                )
                transaction.set(newTxnRef, txnData)
            }.await()

            amount
        }
    }

    override suspend fun refundForOrder(
        userId: String,
        orderId: String,
        amount: Double,
    ): Result<Double> = withContext(ioDispatcher) {
        runCatching {
            if (userId.isBlank() || orderId.isBlank() || amount <= 0.0) return@runCatching 0.0

            val orderRef = firestore.collection("orders").document(orderId)
            val walletDocRef = getWalletDocRef(userId)
            val txnCollectionRef = getTransactionsCollectionRef(userId)

            var refundedAmount = 0.0

            firestore.runTransaction { transaction ->
                val orderSnap = transaction.get(orderRef)
                if (!orderSnap.exists()) return@runTransaction

                val isAlreadyRefunded = orderSnap.getBoolean("walletRefunded") ?: false
                if (isAlreadyRefunded) return@runTransaction

                val walletSnap = transaction.get(walletDocRef)
                val currentBalance = if (walletSnap.exists()) walletSnap.getDouble("balance") ?: 0.0 else 0.0
                val currentLifetimeSpent = if (walletSnap.exists()) walletSnap.getDouble("lifetimeSpent") ?: 0.0 else 0.0
                val now = System.currentTimeMillis()

                val newBalance = currentBalance + amount
                val newLifetimeSpent = (currentLifetimeSpent - amount).coerceAtLeast(0.0)

                if (!walletSnap.exists()) {
                    val walletData = hashMapOf<String, Any>(
                        "balance" to newBalance,
                        "lifetimeAdded" to newBalance,
                        "lifetimeSpent" to 0.0,
                        "createdAt" to now,
                        "updatedAt" to now,
                    )
                    transaction.set(walletDocRef, walletData)
                } else {
                    val updates = hashMapOf<String, Any>(
                        "balance" to newBalance,
                        "lifetimeSpent" to newLifetimeSpent,
                        "updatedAt" to now,
                    )
                    transaction.update(walletDocRef, updates)
                }

                val newTxnRef = txnCollectionRef.document()
                val txnData = hashMapOf<String, Any>(
                    "id" to newTxnRef.id,
                    "type" to "REFUND",
                    "amount" to amount,
                    "description" to "Refund for cancelled Order #${orderId.takeLast(8).uppercase()}",
                    "orderId" to orderId,
                    "razorpayPaymentId" to "",
                    "timestamp" to now,
                )
                transaction.set(newTxnRef, txnData)

                transaction.update(orderRef, mapOf("walletRefunded" to true))

                refundedAmount = amount
            }.await()

            refundedAmount
        }
    }
}
