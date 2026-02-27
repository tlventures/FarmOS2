package com.agriedge.domain.repository

import com.agriedge.data.local.database.entity.SyncQueueItem

/**
 * Repository interface for sync queue operations.
 * 
 * The sync queue enables offline-first architecture by queuing
 * operations for background synchronization when network is available.
 * 
 * Requirements: 36.1, 36.2, 37.1
 */
interface SyncQueueRepository {
    
    /**
     * Add an item to the sync queue.
     */
    suspend fun addToQueue(item: SyncQueueItem)
    
    /**
     * Get all pending items from the sync queue.
     */
    suspend fun getPendingItems(): List<SyncQueueItem>
    
    /**
     * Mark an item as completed after successful sync.
     */
    suspend fun markAsCompleted(id: Long)
    
    /**
     * Mark an item as failed with error message.
     */
    suspend fun markAsFailed(id: Long, error: String)
}
