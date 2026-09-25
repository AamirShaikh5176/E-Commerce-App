package com.example.e_commerceapp.data.data_source.remote.firebase

import android.net.Uri
import com.example.e_commerceapp.domain.entity.user.FirebaseSignInUserEntity
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.mustafaunlu.ecommerce_compose.data.source.remote.FirebaseDataSource
import io.github.nefilim.kjwt.JWT
import io.github.nefilim.kjwt.toJWTKeyID
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
            .addOnSuccessListener {
                val firebaseUser = it.user
                onSuccess(
                    UserInformationEntity(
                        id = firebaseUser?.uid ?: "",
                        name = user.name,
                        email = user.email,
                        phone = user.phone,
                        image = "",
                        password = "",
                        token = createJwtTokenForFirebaseUser(),
                    )
                )
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
            .addOnSuccessListener {
                val firebaseUser = it.user
                onSuccess(
                    UserInformationEntity(
                        id = firebaseUser?.uid ?: "",
                        name = firebaseUser?.displayName ?: "",
                        email = firebaseUser?.email ?: "",
                        phone = firebaseUser?.phoneNumber ?: "",
                        image = firebaseUser?.photoUrl.toString(),
                        password = "",
                        token = createJwtTokenForFirebaseUser(),
                    ),
                )
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
        val userMap = hashMapOf(
            "id" to firebaseAuth.uid,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "image" to user.image,
        )
        val firebaseUser = firebaseAuth.currentUser
        val profileUpdate = userProfileChangeRequest {
            displayName = userMap["name"]
            photoUri = Uri.parse(userMap["image"])  // convert string to Uri
        }
        firebaseUser?.updateProfile(profileUpdate)

        // update user data in firestore
        collection.document(firebaseAuth.uid!!).set(userMap)
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
                        id = snapshot.getString("id") ?: "",
                        name = snapshot.getString("name") ?: "",
                        email = snapshot.getString("email") ?: "",
                        phone = snapshot.getString("phone") ?: "",
                        image = snapshot.getString("image") ?: "",
                        password = "",
                        token = "",
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


    private fun createJwtTokenForFirebaseUser(): String {
        val nowMillis = System.currentTimeMillis() // by mille second
        val nowSeconds = nowMillis / 1000 // by second
        val expirationSeconds = nowSeconds + 1800  // expired after 30 munit

        val jwt = JWT.es256("fb-user123".toJWTKeyID()) {
            claim("iat", nowSeconds)
            claim("exp", expirationSeconds)
        }.encode()

        return jwt
    }
}
