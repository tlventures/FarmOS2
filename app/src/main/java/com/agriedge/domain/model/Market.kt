package com.agriedge.domain.model

data class MarketListing(
    val id: String,
    val productName: String,
    val description: String,
    val category: String,
    val price: Double,
    val currency: String = "INR",
    val quantity: Int,
    val unit: String,
    val sellerId: String,
    val sellerName: String,
    val sellerContact: String,
    val sellerRating: Float? = null,
    val sellerReviewCount: Int = 0,
    val location: String,
    val locationCoordinates: LocationCoordinates? = null,
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String? = null,
    val imageUrl: String? = null, // Deprecated, use imageUrls
    val createdAt: Long = System.currentTimeMillis(),
    val isAvailable: Boolean = true,
    val farmDetails: FarmDetails? = null
)

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class FarmDetails(
    val farmName: String,
    val farmSize: String,
    val organicCertified: Boolean = false,
    val harvestDate: Long? = null
)

enum class ProductCategory {
    SEEDS,
    FERTILIZERS,
    PESTICIDES,
    TOOLS,
    EQUIPMENT,
    PRODUCE,
    OTHER
}
