package com.agriedge.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.agriedge.data.local.database.entity.TreatmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for treatment operations
 * Requirements: 4.1, 15.1
 */
@Dao
interface TreatmentDao {
    
    @Query("SELECT * FROM treatments WHERE diseaseId = :diseaseId AND languageCode = :languageCode")
    fun getTreatmentsForDisease(diseaseId: String, languageCode: String): Flow<List<TreatmentEntity>>
    
    @Query("SELECT * FROM treatments WHERE diseaseId = :diseaseId AND languageCode = :languageCode")
    suspend fun getTreatmentsForDiseaseSync(diseaseId: String, languageCode: String): List<TreatmentEntity>
    
    @Query("SELECT * FROM treatments WHERE id = :id")
    suspend fun getTreatmentById(id: String): TreatmentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(treatment: TreatmentEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(treatments: List<TreatmentEntity>)
    
    @Query("DELETE FROM treatments WHERE cachedAt < :beforeTimestamp")
    suspend fun deleteOldCachedTreatments(beforeTimestamp: Long): Int
}
