package com.agriedge.data.repository

import com.agriedge.data.local.database.dao.SyncQueueDao
import com.agriedge.data.local.database.entity.SyncQueueItem
import com.agriedge.domain.repository.SyncQueueRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SyncQueueRepository using Room database.
 * 
 * This repository handles sync queue operations for offline-first architecture:
 * - Adding items to sync queue
 * - Retrieving pending items for background sync
 * - Updating sync status
 * - Managing retry logic
 * 
 * Requirements: 36.1, 36.2, 37.1
 */
@Singleton
class SyncQueueRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao
) : SyncQueueRepository {
    
    /**
     * Add an item to the sync queue.
     */
    override suspend fun addToQueue(item: SyncQueueItem) {
        syncQueueDao.insert(item)
    }
    
    /**
     * Get all pending items from the sync queue.
     * Items are returned in FIFO order (oldest first).
     */
    override suspend fun getPendingItems(): List<SyncQueueItem> {
        return syncQueueDao.getPendingItems()
    }
    
    /**
     * Mark an item as completed and remove it from the queue.
     */
    override suspend fun markAsCompleted(id: Long) {
        syncQueueDao.updateStatus(id, "COMPLETED")
        // Optionally delete completed items immediately
        syncQueueDao.delete(id)
    }
    
    /**
     * Mark an item as failed with error message.
     * Increments retry count for exponential backoff.
     */
    override suspend fun markAsFailed(id: Long, error: String) {
        syncQueueDao.updateFailure(
            id = id,
            status = "FAILED",
            timestamp = System.currentTimeMillis(),
            error = error
        )
    }
    
    /**
     * Get items that failed but can be retried (retry count < 3).
     */
    suspend fun getFailedItemsForRetry(): List<SyncQueueItem> {
        return syncQueueDao.getFailedItemsForRetry()
    }
    
    /**
     * Get count of pending items as a Flow for reactive UI updates.
     */
    fun getPendingCount(): Flow<Int> {
        return syncQueueDao.getPendingCount()
    }
    
    /**
     * Get pending items by entity type (e.g., only DIAGNOSIS items).
     */
    suspend fun getPendingItemsByType(entityType: String): List<SyncQueueItem> {
        return syncQueueDao.getPendingItemsByType(entityType)
    }
    
    /**
     * Clean up old completed items.
     */
    suspend fun cleanupCompleted(): Int {
        return syncQueueDao.deleteCompleted()
    }
    
    /**
     * Clean up old failed items that exceeded retry limit.
     * @param beforeTimestamp Delete items older than this timestamp
     */
    suspend fun cleanupOldFailedItems(beforeTimestamp: Long): Int {
        return syncQueueDao.deleteOldFailedItems(beforeTimestamp)
    }
}
