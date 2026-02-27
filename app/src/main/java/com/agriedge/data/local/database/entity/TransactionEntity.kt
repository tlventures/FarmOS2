package com.agriedge.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing market transactions locally
 * Requirements: 18.1, 18.2, 18.3, 43.1
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val transactionType: String, // SALE, COLD_STORAGE, EQUIPMENT_RENTAL
    val providerId: String,
    val providerName: String,
    val providerType: String,
    val cropType: String?,
    val quantity: Double?,
    val quantityUnit: String?,
    val pricePerUnit: Double?,
    val totalAmount: Double,
    val status: String, // INITIATED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    val pickupDate: Long?,
    val pickupLocation: String?,
    val contactPerson: String?,
    val contactPhone: String?,
    val createdAt: Long,
    val completedAt: Long?,
    val synced: Boolean = false
)
