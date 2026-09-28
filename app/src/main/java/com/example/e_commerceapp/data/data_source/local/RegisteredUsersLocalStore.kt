package com.example.e_commerceapp.data.data_source.local

import android.content.Context
import android.util.Log
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegisteredUsersLocalStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("registered_users_store", Context.MODE_PRIVATE)
    private val keyCustomers = "registered_customers_json"

    private fun isDesignatedAdminEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val clean = email.trim().lowercase()
        return clean == "admin@ecommerce.com" ||
                clean == "admin@store.com" ||
                clean == "admin@store.in" ||
                clean.startsWith("admin@")
    }

    fun saveUser(user: UserInformationEntity) {
        if (user.email.isBlank()) return
        // Do not add admin users as registered customers
        if (user.role.equals("admin", ignoreCase = true) || isDesignatedAdminEmail(user.email)) {
            return
        }

        try {
            val list = getAllUsers().toMutableList()
            val existingIndex = list.indexOfFirst {
                (user.id.isNotBlank() && it.id == user.id) || it.email.equals(user.email, ignoreCase = true)
            }

            if (existingIndex >= 0) {
                list[existingIndex] = user.copy(
                    id = if (user.id.isNotBlank()) user.id else list[existingIndex].id,
                    name = if (user.name.isNotBlank()) user.name else list[existingIndex].name,
                    phone = if (user.phone.isNotBlank()) user.phone else list[existingIndex].phone,
                    role = "customer"
                )
            } else {
                list.add(0, user.copy(role = "customer"))
            }

            persistList(list)
            Log.d("RegisteredUsersStore", "Saved customer: ${user.email}, total count: ${list.size}")
        } catch (e: Exception) {
            Log.e("RegisteredUsersStore", "Error saving user to local store: ${e.message}", e)
        }
    }

    fun getAllUsers(): List<UserInformationEntity> {
        val result = mutableListOf<UserInformationEntity>()
        try {
            val jsonStr = prefs.getString(keyCustomers, null)
            if (!jsonStr.isNullOrBlank()) {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    result.add(
                        UserInformationEntity(
                            id = obj.optString("id", ""),
                            name = obj.optString("name", "Customer"),
                            email = obj.optString("email", ""),
                            phone = obj.optString("phone", ""),
                            image = obj.optString("image", ""),
                            password = "",
                            token = "",
                            role = obj.optString("role", "customer"),
                        )
                    )
                }
            }

            val curUser = FirebaseAuth.getInstance().currentUser
            if (curUser != null && !curUser.email.isNullOrBlank()) {
                val email = curUser.email!!
                val isNotAdmin = !isDesignatedAdminEmail(email)
                if (isNotAdmin && result.none { it.id == curUser.uid || it.email.equals(email, ignoreCase = true) }) {
                    val entity = UserInformationEntity(
                        id = curUser.uid,
                        name = curUser.displayName?.ifBlank { null }
                            ?: email.substringBefore('@').replaceFirstChar { it.uppercase() },
                        email = email,
                        phone = curUser.phoneNumber ?: "",
                        image = curUser.photoUrl?.toString() ?: "",
                        password = "",
                        token = "",
                        role = "customer",
                    )
                    result.add(0, entity)
                    persistList(result)
                }
            }
        } catch (e: Exception) {
            Log.e("RegisteredUsersStore", "Error reading users from local store: ${e.message}", e)
        }
        return result
    }

    private fun persistList(list: List<UserInformationEntity>) {
        val array = JSONArray()
        for (user in list) {
            val obj = JSONObject().apply {
                put("id", user.id)
                put("name", user.name)
                put("email", user.email)
                put("phone", user.phone)
                put("image", user.image)
                put("role", user.role.ifBlank { "customer" })
            }
            array.put(obj)
        }
        prefs.edit().putString(keyCustomers, array.toString()).apply()
    }
}
