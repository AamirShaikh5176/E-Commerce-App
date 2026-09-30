package com.example.e_commerceapp.ui.screens.supercoins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.model.supercoins.SuperCoinsTransaction
import com.example.e_commerceapp.data.model.supercoins.SuperCoinsWallet
import com.example.e_commerceapp.domain.repository.SuperCoinsRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuperCoinsViewModel @Inject constructor(
    private val superCoinsRepository: SuperCoinsRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _walletState = MutableStateFlow(SuperCoinsWallet())
    val walletState: StateFlow<SuperCoinsWallet> = _walletState.asStateFlow()

    private val _transactionsState = MutableStateFlow<List<SuperCoinsTransaction>>(emptyList())
    val transactionsState: StateFlow<List<SuperCoinsTransaction>> = _transactionsState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var walletJob: Job? = null
    private var transactionsJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        val uid = firebaseAuth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) {
            _walletState.value = SuperCoinsWallet()
            _transactionsState.value = emptyList()
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            // Ensure newly registered or legacy accounts receive their one-time welcome bonus idempotently
            superCoinsRepository.initializeWelcomeBonusIfEligible(uid)

            walletJob?.cancel()
            walletJob = launch {
                superCoinsRepository.getWalletFlow(uid)
                    .catch { _isLoading.value = false }
                    .collect { wallet ->
                        _walletState.value = wallet
                        _isLoading.value = false
                    }
            }

            transactionsJob?.cancel()
            transactionsJob = launch {
                superCoinsRepository.getTransactionsFlow(uid)
                    .catch { }
                    .collect { list ->
                        _transactionsState.value = list
                    }
            }
        }
    }
}
