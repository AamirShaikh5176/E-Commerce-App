package com.example.e_commerceapp.ui.screens.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.domain.entity.address.AddressEntity
import com.example.e_commerceapp.domain.repository.AddressRepository
import com.example.e_commerceapp.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
) : ViewModel() {

    private val _addressesState = MutableStateFlow(ScreenState<List<AddressEntity>>())
    val addressesState: StateFlow<ScreenState<List<AddressEntity>>> = _addressesState

    private val _selectedAddressId = MutableStateFlow<String?>(null)
    val selectedAddressId: StateFlow<String?> = _selectedAddressId

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _addressesState.update { it.copy(isLoading = true, errorMessage = null) }
            addressRepository.getUserAddresses()
                .catch { e ->
                    _addressesState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { list ->
                    _addressesState.update { it.copy(isLoading = false, data = list, errorMessage = null) }
                    // Auto-select default or first address if none selected
                    if (_selectedAddressId.value == null) {
                        val defaultAddr = list.firstOrNull { it.isDefault } ?: list.firstOrNull()
                        _selectedAddressId.value = defaultAddr?.id
                    }
                }
        }
    }

    fun selectAddress(addressId: String) {
        _selectedAddressId.value = addressId
    }

    fun saveAddress(address: AddressEntity, onSuccess: () -> Unit, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val result = addressRepository.saveAddress(address)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to save address")
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            addressRepository.deleteAddress(addressId)
        }
    }

    fun setDefault(addressId: String) {
        viewModelScope.launch {
            addressRepository.setDefaultAddress(addressId)
        }
    }
}
