package com.example.e_commerceapp.data.data_source.remote.firebase

import android.net.Uri
import com.example.e_commerceapp.domain.entity.user.FirebaseSignInUserEntity
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.mustafaunlu.ecommerce_compose.data.source.remote.FirebaseDataSource
import javax.inject.Inject

class FirebaseDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    firestore: FirebaseFirestore,
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
                    firebaseUser.getIdToken(false)
                        .addOnSuccessListener { tokenResult ->
                            onSuccess(
                                UserInformationEntity(
                                    id = firebaseUser.uid,
                                    name = user.name,
                                    email = user.email,
                                    phone = user.phone,
                                    image = "",
                                    password = "",
                                    token = tokenResult.token ?: "",
                                    role = "customer",
                                )
                            )
                        }
                        .addOnFailureListener {
                            onSuccess(
                                UserInformationEntity(
                                    id = firebaseUser.uid,
                                    name = user.name,
                                    email = user.email,
                                    phone = user.phone,
                                    image = "",
                                    password = "",
                                    token = "",
                                    role = "customer",
                                )
                            )
                        }
                } else {
                    onFailure("User creation returned empty user")
                }
            }.addOnFailureListener {
                onFailure(it.message ?: "An error occurred")
            }
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
                    firebaseUser.getIdToken(false)
                        .addOnSuccessListener { tokenResult ->
                            val claims = tokenResult.claims
                            val isAdmin = claims["admin"] == true || claims["role"] == "admin"
                            onSuccess(
                                UserInformationEntity(
                                    id = firebaseUser.uid,
                                    name = firebaseUser.displayName ?: "",
                                    email = firebaseUser.email ?: "",
                                    phone = firebaseUser.phoneNumber ?: "",
                                    image = firebaseUser.photoUrl?.toString() ?: "",
                                    password = "",
                                    token = tokenResult.token ?: "",
                                    role = if (isAdmin) "admin" else "customer",
                                )
                            )
                        }
                        .addOnFailureListener {
                            onSuccess(
                                UserInformationEntity(
                                    id = firebaseUser.uid,
                                    name = firebaseUser.displayName ?: "",
                                    email = firebaseUser.email ?: "",
                                    phone = firebaseUser.phoneNumber ?: "",
                                    image = firebaseUser.photoUrl?.toString() ?: "",
                                    password = "",
                                    token = "",
                                    role = "customer",
                                )
                            )
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
        val profileUpdate = userProfileChangeRequest {
            displayName = user.name
            if (user.image.isNotBlank()) {
                photoUri = Uri.parse(user.image)
            }
        }
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
        user.getIdToken(true) // force refresh ID token from Firebase backend
            .addOnSuccessListener { tokenResult ->
                val claims = tokenResult.claims
                val isAdminClaim = claims["admin"] == true || claims["role"] == "admin"
                if (isAdminClaim) {
                    onResult(true)
                } else {
                    // Check Firestore role as secondary verification
                    collection.document(user.uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role")
                            onResult(role == "admin")
                        }
                        .addOnFailureListener {
                            onResult(false)
                        }
                }
            }
            .addOnFailureListener {
                collection.document(user.uid).get()
                    .addOnSuccessListener { doc ->
                        val role = doc.getString("role")
                        onResult(role == "admin")
                    }
                    .addOnFailureListener {
                        onResult(false)
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
