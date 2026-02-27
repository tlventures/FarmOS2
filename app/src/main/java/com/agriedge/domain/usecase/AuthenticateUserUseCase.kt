package com.agriedge.domain.usecase

import com.agriedge.domain.model.UserProfile

/**
 * Use case for user authentication
 */
interface AuthenticateUserUseCase {
    
    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): Result<UserProfile?>
    
    /**
     * Login with phone and password
     */
    suspend fun login(phone: String, password: String): Result<UserProfile>
    
    /**
     * Register a new user
     */
    suspend fun register(
        name: String,
        phone: String,
        email: String,
        location: String,
        password: String
    ): Result<UserProfile>
    
    /**
     * Logout user
     */
    suspend fun logout()
}
