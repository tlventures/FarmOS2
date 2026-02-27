package com.agriedge.domain.repository

import com.agriedge.domain.model.UserProfile

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    
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
