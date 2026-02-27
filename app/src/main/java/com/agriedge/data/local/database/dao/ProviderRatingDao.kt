package com.agriedge.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.agriedge.data.local.database.entity.ProviderRatingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for provider rating operations
 * Requirements: 31.1, 31.3
 */
@Dao
interface ProviderRatingDao {
    
    @Query("SELECT * FROM provider_ratings WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllRatings(userId: String): Flow<List<ProviderRatingEntity>>
    
    @Query("SELECT * FROM provider_ratings WHERE providerId = :providerId ORDER BY createdAt DESC")
    fun getRatingsForProvider(providerId: String): Flow<List<ProviderRatingEntity>>
    
    @Query("SELECT * FROM provider_ratings WHERE transactionId = :transactionId")
    suspend fun getRatingForTransaction(transactionId: String): ProviderRatingEntity?
    
    @Query("SELECT * FROM provider_ratings WHERE userId = :userId AND synced = 0")
    suspend fun getUnsyncedRatings(userId: String): List<ProviderRatingEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rating: ProviderRatingEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ratings: List<ProviderRatingEntity>)
    
    @Query("UPDATE provider_ratings SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
