package com.agriedge.domain.model

import java.util.UUID

/**
 * Domain model representing a market transaction
 * Requirements: 16.1, 18.1, 18.2, 18.3
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val transactionType: TransactionType,
    val provider: Provider,
    val cropType: CropType?,
    val quantity: Quantity?,
    val pricePerUnit: Double?,
    val totalAmount: Double,
    val status: TransactionStatus,
    val pickupDetails: PickupDetails?,
    val createdAt: Long,
    val completedAt: Long?,
    val synced: Boolean = false
)

enum class TransactionType {
    SALE,
    COLD_STORAGE,
    EQUIPMENT_RENTAL
}

enum class TransactionStatus {
    INITIATED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

data class PickupDetails(
    val date: Long,
    val location: String,
    val contactPerson: String,
    val contactPhone: String
)

data class Quantity(
    val value: Double,
    val unit: QuantityUnit
)

enum class QuantityUnit {
    KILOGRAM,
    QUINTAL,
    TON
}
