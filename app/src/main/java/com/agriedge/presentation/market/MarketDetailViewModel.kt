package com.agriedge.presentation.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.model.MarketListing
import com.agriedge.domain.repository.MarketRepository
import com.agriedge.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketDetailUiState(
    val listing: MarketListing? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val transactionInitiated: Boolean = false
)

@HiltViewModel
class MarketDetailViewModel @Inject constructor(
    private val marketRepository: MarketRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketDetailUiState())
    val uiState: StateFlow<MarketDetailUiState> = _uiState.asStateFlow()

    fun loadListing(listingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            (marketRepository as com.agriedge.data.repository.MarketRepositoryImpl)
                .getListingById(listingId)
                .onSuccess { listing ->
                    _uiState.value = _uiState.value.copy(
                        listing = listing,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load listing"
                    )
                }
        }
    }

    fun initiateTransaction(listing: com.agriedge.domain.model.MarketListing, quantity: Int) {
        viewModelScope.launch {
            (transactionRepository as com.agriedge.data.repository.TransactionRepositoryImpl)
                .createTransaction(
                    listingId = listing.id,
                    sellerId = listing.sellerId,
                    quantity = quantity,
                    totalAmount = listing.price * quantity
                ).onSuccess {
                    _uiState.value = _uiState.value.copy(transactionInitiated = true)
                }.onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to initiate transaction"
                    )
                }
        }
    }

    fun resetTransactionState() {
        _uiState.value = _uiState.value.copy(transactionInitiated = false)
    }
}
