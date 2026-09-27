package com.example.e_commerceapp.data.repository

import android.content.Context
import android.util.Log
import com.example.e_commerceapp.domain.entity.address.AddressEntity
import com.example.e_commerceapp.domain.repository.AddressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context,
) : AddressRepository {

    private val prefs = context.getSharedPreferences("user_addresses_store", Context.MODE_PRIVATE)

    private fun getStorageKey(uid: String?): String {
        return "addresses_${uid ?: "guest"}"
    }

    private fun loadLocalAddresses(uid: String?): List<AddressEntity> {
        val list = mutableListOf<AddressEntity>()
        val jsonStr = prefs.getString(getStorageKey(uid), null) ?: return list
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AddressEntity(
                        id = obj.optString("id", ""),
                        fullName = obj.optString("fullName", ""),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        houseFlatNumber = obj.optString("houseFlatNumber", ""),
                        streetArea = obj.optString("streetArea", ""),
                        landmark = obj.optString("landmark", ""),
                        city = obj.optString("city", ""),
                        state = obj.optString("state", ""),
                        pinCode = obj.optString("pinCode", ""),
                        addressType = obj.optString("addressType", "HOME"),
                        isDefault = obj.optBoolean("isDefault", false),
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("AddressRepo", "Error parsing local addresses: ${e.message}")
        }
        return list
    }

    private fun saveLocalAddresses(uid: String?, list: List<AddressEntity>) {
        try {
            val array = JSONArray()
            for (addr in list) {
                val obj = JSONObject().apply {
                    put("id", addr.id)
                    put("fullName", addr.fullName)
                    put("phoneNumber", addr.phoneNumber)
                    put("houseFlatNumber", addr.houseFlatNumber)
                    put("streetArea", addr.streetArea)
                    put("landmark", addr.landmark)
                    put("city", addr.city)
                    put("state", addr.state)
                    put("pinCode", addr.pinCode)
                    put("addressType", addr.addressType)
                    put("isDefault", addr.isDefault)
                }
                array.put(obj)
            }
            prefs.edit().putString(getStorageKey(uid), array.toString()).apply()
        } catch (e: Exception) {
            Log.e("AddressRepo", "Error saving local addresses: ${e.message}")
        }
    }

    override fun getUserAddresses(): Flow<List<AddressEntity>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        // Send local addresses immediately
        val initialList = loadLocalAddresses(uid)
        trySend(initialList)

        // Also listen to remote Firestore changes if available
        val listener = firestore.collection("users")
            .document(uid)
            .collection("addresses")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(loadLocalAddresses(uid))
                    return@addSnapshotListener
                }

                val remoteList = snapshot.documents.mapNotNull { doc ->
                    AddressEntity(
                        id = doc.id,
                        fullName = doc.getString("fullName") ?: "",
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        houseFlatNumber = doc.getString("houseFlatNumber") ?: "",
                        streetArea = doc.getString("streetArea") ?: "",
                        landmark = doc.getString("landmark") ?: "",
                        city = doc.getString("city") ?: "",
                        state = doc.getString("state") ?: "",
                        pinCode = doc.getString("pinCode") ?: "",
                        addressType = doc.getString("addressType") ?: "HOME",
                        isDefault = doc.getBoolean("isDefault") ?: false
                    )
                }

                if (remoteList.isNotEmpty()) {
                    saveLocalAddresses(uid, remoteList)
                    trySend(remoteList)
                } else {
                    trySend(loadLocalAddresses(uid))
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun saveAddress(address: AddressEntity): Result<Unit> = runCatching {
        val addressId = address.id.ifBlank { UUID.randomUUID().toString() }
        val toSave = address.copy(id = addressId)
        val uid = auth.currentUser?.uid

        // 1. Immediately persist locally
        val currentList = loadLocalAddresses(uid).toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == addressId }
        if (existingIndex >= 0) {
            currentList[existingIndex] = toSave
        } else {
            currentList.add(toSave)
        }

        // If this address is set as default, unset others
        if (toSave.isDefault) {
            currentList.indices.forEach { i ->
                if (currentList[i].id != addressId) {
                    currentList[i] = currentList[i].copy(isDefault = false)
                }
            }
        }
        saveLocalAddresses(uid, currentList)

        // 2. Sync to Firestore with non-blocking timeout
        if (!uid.isNullOrBlank()) {
            val map = hashMapOf(
                "id" to addressId,
                "fullName" to toSave.fullName,
                "phoneNumber" to toSave.phoneNumber,
                "houseFlatNumber" to toSave.houseFlatNumber,
                "streetArea" to toSave.streetArea,
                "landmark" to toSave.landmark,
                "city" to toSave.city,
                "state" to toSave.state,
                "pinCode" to toSave.pinCode,
                "addressType" to toSave.addressType,
                "isDefault" to toSave.isDefault
            )

            try {
                withTimeoutOrNull(1500L) {
                    firestore.collection("users")
                        .document(uid)
                        .collection("addresses")
                        .document(addressId)
                        .set(map)
                        .await()
                }
            } catch (e: Exception) {
                Log.w("AddressRepo", "Firestore save address offline or failed: ${e.message}")
            }
        }
    }

    override suspend fun deleteAddress(addressId: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid
        val currentList = loadLocalAddresses(uid).toMutableList()
        currentList.removeAll { it.id == addressId }
        saveLocalAddresses(uid, currentList)

        if (!uid.isNullOrBlank()) {
            try {
                withTimeoutOrNull(1500L) {
                    firestore.collection("users")
                        .document(uid)
                        .collection("addresses")
                        .document(addressId)
                        .delete()
                        .await()
                }
            } catch (e: Exception) {
                Log.w("AddressRepo", "Firestore delete address offline: ${e.message}")
            }
        }
    }

    override suspend fun setDefaultAddress(addressId: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid
        val currentList = loadLocalAddresses(uid).toMutableList()
        currentList.indices.forEach { i ->
            currentList[i] = currentList[i].copy(isDefault = currentList[i].id == addressId)
        }
        saveLocalAddresses(uid, currentList)

        if (!uid.isNullOrBlank()) {
            try {
                withTimeoutOrNull(1500L) {
                    val otherDocs = firestore.collection("users")
                        .document(uid)
                        .collection("addresses")
                        .get()
                        .await()

                    for (doc in otherDocs.documents) {
                        doc.reference.update("isDefault", doc.id == addressId).await()
                    }
                }
            } catch (e: Exception) {
                Log.w("AddressRepo", "Firestore setDefault offline: ${e.message}")
            }
        }
    }
}
