package com.agriedge.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agriedge.data.local.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for transaction operations
 * Requirements: 18.1, 18.3
 */
@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND status = :status")
    fun getTransactionsByStatus(userId: String, status: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND synced = 0")
    suspend fun getUnsyncedTransactions(userId: String): List<TransactionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)
    
    @Update
    suspend fun update(transaction: TransactionEntity)
    
    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
    
    @Query("UPDATE transactions SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
