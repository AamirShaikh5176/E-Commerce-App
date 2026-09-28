package com.example.e_commerceapp.data.data_source.remote.firebase

import android.net.Uri
import com.example.e_commerceapp.domain.entity.user.FirebaseSignInUserEntity
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.mustafaunlu.ecommerce_compose.data.source.remote.FirebaseDataSource
import javax.inject.Inject

class FirebaseDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    firestore: FirebaseFirestore,
    private val registeredUsersLocalStore: com.example.e_commerceapp.data.data_source.local.RegisteredUsersLocalStore,
) : FirebaseDataSource {

    private val collection = firestore.collection("users")

    override fun signUpWithFirebase(
        user: UserInformationEntity,
        onSuccess: (UserInformationEntity) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        firebaseAuth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(user.name)
                        .build()
                    firebaseUser.updateProfile(profileUpdate)

                    val userEntity = UserInformationEntity(
                        id = firebaseUser.uid,
                        name = user.name,
                        email = user.email,
                        phone = user.phone,
                        image = "",
                        password = "",
                        token = "",
                        role = "customer",
                    )

                    // 1. Persist registered customer locally so Admin immediately sees them
                    registeredUsersLocalStore.saveUser(userEntity)

                    // 2. Also write customer profile document to Firestore
                    val userMap = hashMapOf<String, Any?>(
                        "id" to firebaseUser.uid,
                        "name" to user.name,
                        "email" to user.email,
                        "phone" to user.phone,
                        "image" to "",
                        "role" to "customer",
                        "createdAt" to System.currentTimeMillis()
                    )
                    collection.document(firebaseUser.uid).set(userMap)

                    firebaseUser.getIdToken(false)
                        .addOnSuccessListener { tokenResult ->
                            onSuccess(userEntity.copy(token = tokenResult.token ?: ""))
                        }
                        .addOnFailureListener {
                            onSuccess(userEntity)
                        }
                } else {
                    onFailure("User creation returned empty user")
                }
            }.addOnFailureListener {
                onFailure(it.message ?: "An error occurred")
            }
    }

    private fun isDesignatedAdminEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val clean = email.trim().lowercase()
        return clean == "admin@ecommerce.com" ||
                clean == "admin@store.com" ||
                clean == "admin@store.in" ||
                clean.startsWith("admin@")
    }

    override fun signInWithFirebase(
        user: FirebaseSignInUserEntity,
        onSuccess: (UserInformationEntity) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        firebaseAuth.signInWithEmailAndPassword(user.email, user.password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userEmail = firebaseUser.email ?: user.email
                    val isAdminEmail = isDesignatedAdminEmail(userEmail)
                    val customerEntity = UserInformationEntity(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName?.ifBlank { null } ?: user.email.substringBefore('@').replaceFirstChar { it.uppercase() },
                        email = userEmail,
                        phone = firebaseUser.phoneNumber ?: "",
                        image = firebaseUser.photoUrl?.toString() ?: "",
                        password = "",
                        token = "",
                        role = if (isAdminEmail) "admin" else "customer",
                    )
                    if (!isAdminEmail) {
                        registeredUsersLocalStore.saveUser(customerEntity)
                    }

                    // Ensure customer document exists in Firestore
                    collection.document(firebaseUser.uid).get()
                        .addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                val userMap = hashMapOf<String, Any?>(
                                    "id" to firebaseUser.uid,
                                    "name" to customerEntity.name,
                                    "email" to customerEntity.email,
                                    "phone" to customerEntity.phone,
                                    "image" to customerEntity.image,
                                    "role" to customerEntity.role,
                                    "createdAt" to System.currentTimeMillis()
                                )
                                collection.document(firebaseUser.uid).set(userMap)
                            }
                        }

                    firebaseUser.getIdToken(false)
                        .addOnSuccessListener { tokenResult ->
                            val claims = tokenResult.claims
                            val isAdmin = isAdminEmail || claims["admin"] == true || claims["role"] == "admin"
                            onSuccess(
                                customerEntity.copy(
                                    token = tokenResult.token ?: "",
                                    role = if (isAdmin) "admin" else "customer",
                                )
                            )
                        }
                        .addOnFailureListener {
                            onSuccess(customerEntity)
                        }
                } else {
                    onFailure("Sign in returned empty user")
                }
            }.addOnFailureListener {
                onFailure(it.message ?: "An error occurred")
            }
    }

    override fun forgotPassword(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {
                onFailure(it.message ?: "An error occurred")
            }
    }

    override fun writeUserDataToFirebase(
        user: UserInformationEntity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val uid = firebaseAuth.uid
        if (uid == null) {
            onFailure("User is not authenticated")
            return
        }

        val userMap = hashMapOf<String, Any?>(
            "id" to uid,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "image" to user.image,
            "role" to user.role.ifBlank { "customer" },
        )
        val firebaseUser = firebaseAuth.currentUser
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(user.name)
            .apply {
                if (user.image.isNotBlank()) {
                    setPhotoUri(Uri.parse(user.image))
                }
            }
            .build()
        firebaseUser?.updateProfile(profileUpdate)

        // write user data to firestore
        collection.document(uid).set(userMap)
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {
                onFailure(it.message ?: "An error occurred")
            }
    }

    override fun readUserDataFromFirebase(
        userId: String,
        onSuccess: (UserInformationEntity) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        collection.document(userId).get()
            .addOnSuccessListener { snapshot ->
                onSuccess(
                    UserInformationEntity(
                        id = snapshot.getString("id") ?: userId,
                        name = snapshot.getString("name") ?: "",
                        email = snapshot.getString("email") ?: "",
                        phone = snapshot.getString("phone") ?: "",
                        image = snapshot.getString("image") ?: "",
                        password = "",
                        token = "",
                        role = snapshot.getString("role") ?: "customer",
                    ),
                )
            }.addOnFailureListener {
                onFailure(it.message ?: "An error occurred")
            }
    }

    override fun logoutFromFirebase(onSuccess: () -> Unit) {
        firebaseAuth.signOut()
        onSuccess()
    }

    override fun verifyAdminClaim(onResult: (Boolean) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            onResult(false)
            return
        }
        val isAdminEmail = isDesignatedAdminEmail(user.email)
        user.getIdToken(true) // force refresh ID token from Firebase backend
            .addOnSuccessListener { tokenResult ->
                val claims = tokenResult.claims
                val isAdminClaim = claims["admin"] == true || claims["role"] == "admin"
                if (isAdminClaim || isAdminEmail) {
                    onResult(true)
                } else {
                    // Check Firestore role as secondary verification
                    collection.document(user.uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role")
                            onResult(role.equals("admin", ignoreCase = true))
                        }
                        .addOnFailureListener {
                            onResult(false)
                        }
                }
            }
            .addOnFailureListener {
                if (isAdminEmail) {
                    onResult(true)
                } else {
                    collection.document(user.uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role")
                            onResult(role.equals("admin", ignoreCase = true))
                        }
                        .addOnFailureListener {
                            onResult(false)
                        }
                }
            }
    }

    override fun getNativeIdToken(forceRefresh: Boolean, onResult: (String?) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            onResult(null)
            return
        }
        user.getIdToken(forceRefresh)
            .addOnSuccessListener { result ->
                onResult(result.token)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
