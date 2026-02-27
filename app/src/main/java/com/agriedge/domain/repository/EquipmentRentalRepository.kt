package com.agriedge.domain.repository

import com.agriedge.domain.model.EquipmentRental
import com.agriedge.domain.model.EquipmentType

interface EquipmentRentalRepository {
    suspend fun searchEquipment(
        latitude: Double,
        longitude: Double,
        radius: Int,
        equipmentType: EquipmentType?,
        startDate: Long,
        endDate: Long
    ): Result<List<EquipmentRental>>
    
    suspend fun getEquipmentDetails(id: String): Result<EquipmentRental>
    
    suspend fun bookEquipment(
        equipmentId: String,
        startDate: Long,
        endDate: Long,
        deliveryRequired: Boolean
    ): Result<String> // Returns booking ID
}
