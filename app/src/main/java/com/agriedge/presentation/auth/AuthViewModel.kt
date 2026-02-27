package com.agriedge.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriedge.domain.model.UserProfile
import com.agriedge.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: UserProfile? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Check if user is already logged in
            val result = authRepository.getCurrentUser()
            
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = user != null,
                        currentUser = user
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = false
                    )
                }
            )
        }
    }
    
    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authRepository.login(phone, password)
            
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = user,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        error = error.message ?: "Login failed"
                    )
                }
            )
        }
    }
    
    fun register(
        name: String,
        phone: String,
        email: String,
        location: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authRepository.register(name, phone, email, location, password)
            
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = user,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        error = error.message ?: "Registration failed"
                    )
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState(isAuthenticated = false)
        }
    }
}
