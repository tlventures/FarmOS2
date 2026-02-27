package com.agriedge.domain.model

/**
 * Domain model representing a user profile
 */
data class UserProfile(
    val userId: String,
    val name: String,
    val phoneNumber: String,
    val email: String?,
    val location: String,
    val createdAt: Long
)
