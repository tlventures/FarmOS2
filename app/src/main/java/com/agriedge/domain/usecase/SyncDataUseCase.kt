package com.agriedge.domain.usecase

/**
 * Use case for synchronizing data with backend
 * Requirements: 36.1, 37.1, 37.2
 */
interface SyncDataUseCase {
    
    /**
     * Sync all pending data to backend
     * 
     * @param userId The user ID
     * @return Result containing sync status
     */
    suspend operator fun invoke(userId: String): Result<SyncStatus>
}

data class SyncStatus(
    val totalItems: Int,
    val syncedItems: Int,
    val failedItems: Int,
    val errors: List<String>
)
