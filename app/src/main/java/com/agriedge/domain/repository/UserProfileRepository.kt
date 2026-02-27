package com.agriedge.domain.repository

import com.agriedge.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user profile data operations
 * Requirements: 34.1, 34.2, 34.3, 34.4
 */
interface UserProfileRepository {
    
    /**
     * Get user profile as a reactive Flow
     */
    fun getUserProfile(userId: String): Flow<UserProfile?>
    
    /**
     * Save or update user profile
     */
    suspend fun saveUserProfile(profile: UserProfile)
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(profile: UserProfile)
    
    /**
     * Delete user profile
     */
    suspend fun deleteUserProfile(userId: String)
    
    /**
     * Sync profile to cloud storage
     */
    suspend fun syncToCloud(profile: UserProfile): Result<Unit>
}
