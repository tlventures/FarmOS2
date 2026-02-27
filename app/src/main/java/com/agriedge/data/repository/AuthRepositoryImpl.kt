package com.agriedge.data.repository

import com.agriedge.domain.model.UserProfile
import com.agriedge.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    
    private var currentUser: UserProfile? = null
    private val mockUsers = mutableMapOf<String, MockUser>()
    
    data class MockUser(
        val name: String,
        val phone: String,
        val email: String?,
        val location: String,
        val password: String
    )
    
    override suspend fun getCurrentUser(): Result<UserProfile?> {
        return try {
            delay(300) // Simulate network delay
            Result.success(currentUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun login(phone: String, password: String): Result<UserProfile> {
        return try {
            delay(500) // Simulate network delay
            
            val mockUser = mockUsers[phone]
            
            if (mockUser == null) {
                return Result.failure(Exception("User not found. Please register first."))
            }
            
            if (mockUser.password != password) {
                return Result.failure(Exception("Invalid password"))
            }
            
            val user = UserProfile(
                userId = "user_${phone.hashCode()}",
                name = mockUser.name,
                phoneNumber = mockUser.phone,
                email = mockUser.email,
                location = mockUser.location,
                createdAt = System.currentTimeMillis()
            )
            
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        location: String,
        password: String
    ): Result<UserProfile> {
        return try {
            delay(500) // Simulate network delay
            
            if (mockUsers.containsKey(phone)) {
                return Result.failure(Exception("Phone number already registered"))
            }
            
            mockUsers[phone] = MockUser(
                name = name,
                phone = phone,
                email = email.ifEmpty { null },
                location = location,
                password = password
            )
            
            val user = UserProfile(
                userId = "user_${phone.hashCode()}",
                name = name,
                phoneNumber = phone,
                email = email.ifEmpty { null },
                location = location,
                createdAt = System.currentTimeMillis()
            )
            
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout() {
        currentUser = null
    }
}
