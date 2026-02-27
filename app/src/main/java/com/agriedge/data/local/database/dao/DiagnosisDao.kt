package com.agriedge.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agriedge.data.local.database.entity.DiagnosisEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for diagnosis operations
 * Requirements: 5.1, 8.1, 8.2
 */
@Dao
interface DiagnosisDao {
    
    @Query("SELECT * FROM diagnoses WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllDiagnoses(userId: String): Flow<List<DiagnosisEntity>>
    
    @Query("SELECT * FROM diagnoses WHERE id = :id")
    suspend fun getDiagnosisById(id: String): DiagnosisEntity?
    
    @Query("SELECT * FROM diagnoses WHERE userId = :userId AND synced = 0")
    suspend fun getUnsyncedDiagnoses(userId: String): List<DiagnosisEntity>
    
    @Query("SELECT COUNT(*) FROM diagnoses WHERE userId = :userId")
    suspend fun getDiagnosisCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diagnosis: DiagnosisEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(diagnoses: List<DiagnosisEntity>)
    
    @Update
    suspend fun update(diagnosis: DiagnosisEntity)
    
    @Delete
    suspend fun delete(diagnosis: DiagnosisEntity)
    
    @Query("DELETE FROM diagnoses WHERE userId = :userId AND timestamp < :beforeTimestamp")
    suspend fun deleteOldDiagnoses(userId: String, beforeTimestamp: Long): Int
    
    @Query("UPDATE diagnoses SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
