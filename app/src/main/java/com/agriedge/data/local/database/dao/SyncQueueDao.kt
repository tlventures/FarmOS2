package com.agriedge.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agriedge.data.local.database.entity.SyncQueueItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for sync queue operations
 * Requirements: 36.1, 36.2, 37.1
 */
@Dao
interface SyncQueueDao {
    
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingItems(): List<SyncQueueItem>
    
    @Query("SELECT * FROM sync_queue WHERE status = 'FAILED' AND retryCount < 3 ORDER BY timestamp ASC")
    suspend fun getFailedItemsForRetry(): List<SyncQueueItem>
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED'")
    fun getPendingCount(): Flow<Int>
    
    @Query("SELECT * FROM sync_queue WHERE entityType = :entityType AND status = 'PENDING'")
    suspend fun getPendingItemsByType(entityType: String): List<SyncQueueItem>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueItem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SyncQueueItem>)
    
    @Update
    suspend fun update(item: SyncQueueItem)
    
    @Query("UPDATE sync_queue SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
    
    @Query("UPDATE sync_queue SET status = :status, retryCount = retryCount + 1, lastAttemptAt = :timestamp, errorMessage = :error WHERE id = :id")
    suspend fun updateFailure(id: Long, status: String, timestamp: Long, error: String)
    
    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: Long)
    
    @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED'")
    suspend fun deleteCompleted(): Int
    
    @Query("DELETE FROM sync_queue WHERE status = 'FAILED' AND retryCount >= 3 AND lastAttemptAt < :beforeTimestamp")
    suspend fun deleteOldFailedItems(beforeTimestamp: Long): Int
}
