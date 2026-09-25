package com.example.e_commerceapp.domain.repository

import com.example.e_commerceapp.domain.entity.address.AddressEntity
import kotlinx.coroutines.flow.Flow

interface AddressRepository {
    fun getUserAddresses(): Flow<List<AddressEntity>>
    suspend fun saveAddress(address: AddressEntity): Result<Unit>
    suspend fun deleteAddress(addressId: String): Result<Unit>
    suspend fun setDefaultAddress(addressId: String): Result<Unit>
}
