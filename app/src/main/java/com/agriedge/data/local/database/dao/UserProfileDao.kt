package com.agriedge.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agriedge.data.local.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for user profile operations
 * Requirements: 34.1, 34.2, 34.3
 */
@Dao
interface UserProfileDao {
    
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>
    
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    suspend fun getUserProfileSync(userId: String): UserProfileEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity): Long
    
    @Update
    suspend fun update(profile: UserProfileEntity)
    
    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun delete(userId: String)
    
    @Query("UPDATE user_profile SET lastSyncedAt = :timestamp WHERE userId = :userId")
    suspend fun updateLastSyncedAt(userId: String, timestamp: Long)
}
