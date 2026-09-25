package com.example.e_commerceapp.data.repository

import com.example.e_commerceapp.domain.entity.address.AddressEntity
import com.example.e_commerceapp.domain.repository.AddressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AddressRepository {

    private val sampleAddresses = listOf(
        AddressEntity(
            id = "default_sample_1",
            fullName = "Rahul Sharma",
            phoneNumber = "9876543210",
            houseFlatNumber = "Flat 402, Lotus Residency",
            streetArea = "100 Feet Road, Indiranagar",
            landmark = "Near Metro Pillar 84",
            city = "Bengaluru",
            state = "Karnataka",
            pinCode = "560038",
            addressType = "HOME",
            isDefault = true
        ),
        AddressEntity(
            id = "default_sample_2",
            fullName = "Rahul Sharma (Office)",
            phoneNumber = "9876543210",
            houseFlatNumber = "Tech Park Tower B, 5th Floor",
            streetArea = "Outer Ring Road, Bellandur",
            landmark = "Opposite EcoSpace",
            city = "Bengaluru",
            state = "Karnataka",
            pinCode = "560103",
            addressType = "WORK",
            isDefault = false
        )
    )

    // In-memory fallback if guest or offline
    private val inMemoryAddresses = sampleAddresses.toMutableList()

    override fun getUserAddresses(): Flow<List<AddressEntity>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(inMemoryAddresses.toList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(uid)
            .collection("addresses")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(inMemoryAddresses.toList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
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

                if (list.isEmpty()) {
                    trySend(inMemoryAddresses.toList())
                } else {
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun saveAddress(address: AddressEntity): Result<Unit> = runCatching {
        val addressId = address.id.ifBlank { UUID.randomUUID().toString() }
        val toSave = address.copy(id = addressId)

        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            val existingIndex = inMemoryAddresses.indexOfFirst { it.id == addressId }
            if (existingIndex >= 0) {
                inMemoryAddresses[existingIndex] = toSave
            } else {
                inMemoryAddresses.add(toSave)
            }
            if (toSave.isDefault) {
                inMemoryAddresses.indices.forEach { i ->
                    if (inMemoryAddresses[i].id != addressId) {
                        inMemoryAddresses[i] = inMemoryAddresses[i].copy(isDefault = false)
                    }
                }
            }
            return@runCatching
        }

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

        firestore.collection("users")
            .document(uid)
            .collection("addresses")
            .document(addressId)
            .set(map)
            .await()

        if (toSave.isDefault) {
            // Reset other addresses default flag
            val otherDocs = firestore.collection("users")
                .document(uid)
                .collection("addresses")
                .get()
                .await()

            for (doc in otherDocs.documents) {
                if (doc.id != addressId && doc.getBoolean("isDefault") == true) {
                    doc.reference.update("isDefault", false).await()
                }
            }
        }
    }

    override suspend fun deleteAddress(addressId: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            inMemoryAddresses.removeAll { it.id == addressId }
            return@runCatching
        }

        firestore.collection("users")
            .document(uid)
            .collection("addresses")
            .document(addressId)
            .delete()
            .await()
    }

    override suspend fun setDefaultAddress(addressId: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            inMemoryAddresses.indices.forEach { i ->
                inMemoryAddresses[i] = inMemoryAddresses[i].copy(isDefault = inMemoryAddresses[i].id == addressId)
            }
            return@runCatching
        }

        val allDocs = firestore.collection("users")
            .document(uid)
            .collection("addresses")
            .get()
            .await()

        for (doc in allDocs.documents) {
            doc.reference.update("isDefault", doc.id == addressId).await()
        }
    }
}
