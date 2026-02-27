package com.agriedge.domain.repository

import com.agriedge.domain.model.Transaction
import com.agriedge.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for transaction data operations
 * Requirements: 18.1, 18.2, 18.3
 */
interface TransactionRepository {
    
    /**
     * Get all transactions for a user as a reactive Flow
     */
    fun getAllTransactions(userId: String): Flow<List<Transaction>>
    
    /**
     * Get a specific transaction by ID
     */
    suspend fun getTransactionById(id: String): Transaction?
    
    /**
     * Save a new transaction
     */
    suspend fun saveTransaction(transaction: Transaction)
    
    /**
     * Update transaction status
     */
    suspend fun updateTransactionStatus(id: String, status: TransactionStatus)
    
    /**
     * Initiate a transaction through Beckn protocol
     */
    suspend fun initiateTransaction(transaction: Transaction): Result<Transaction>
    
    /**
     * Get transaction status from remote
     */
    suspend fun getTransactionStatus(id: String): Result<TransactionStatus>
    
    /**
     * Cancel a transaction
     */
    suspend fun cancelTransaction(id: String, reason: String): Result<Unit>
}
