package com.agriedge.domain.usecase

import com.agriedge.domain.model.Transaction

/**
 * Use case for initiating market transactions
 * Requirements: 18.1, 18.2
 */
interface InitiateTransactionUseCase {
    
    /**
     * Initiate a transaction with a provider
     * 
     * @param transaction The transaction details
     * @return Result containing the initiated transaction with ID
     */
    suspend operator fun invoke(transaction: Transaction): Result<Transaction>
}
