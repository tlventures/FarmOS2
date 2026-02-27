package com.agriedge.presentation.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.model.MarketListing
import com.agriedge.domain.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketUiState(
    val listings: List<MarketListing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    private var allListings: List<MarketListing> = emptyList()

    init {
        loadListings()
    }

    fun loadListings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            (marketRepository as com.agriedge.data.repository.MarketRepositoryImpl)
                .getMarketListings()
                .onSuccess { listings ->
                    allListings = listings
                    _uiState.value = _uiState.value.copy(
                        listings = listings,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load listings"
                    )
                }
        }
    }

    fun searchListings(query: String) {
        val filtered = if (query.isBlank()) {
            allListings
        } else {
            allListings.filter {
                it.productName.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = _uiState.value.copy(listings = filtered)
    }

    fun filterByCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        
        val filtered = if (category == null) {
            allListings
        } else {
            allListings.filter { it.category == category }
        }
        _uiState.value = _uiState.value.copy(listings = filtered)
    }
}
