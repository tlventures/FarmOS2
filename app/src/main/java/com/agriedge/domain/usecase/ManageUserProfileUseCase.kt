package com.agriedge.domain.usecase

import com.agriedge.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing user profile
 * Requirements: 34.1, 34.2, 34.3
 */
interface ManageUserProfileUseCase {
    
    /**
     * Get user profile as a reactive Flow
     */
    fun getProfile(userId: String): Flow<UserProfile?>
    
    /**
     * Create or update user profile
     */
    suspend fun saveProfile(profile: UserProfile): Result<Unit>
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(profile: UserProfile): Result<Unit>
    
    /**
     * Delete user profile
     */
    suspend fun deleteProfile(userId: String): Result<Unit>
}
