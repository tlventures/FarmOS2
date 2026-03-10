package com.agriedge.data.repository

import com.agriedge.data.remote.NetworkErrorHandler
import com.agriedge.data.remote.TokenManager
import com.agriedge.data.remote.api.AuthApiService
import com.agriedge.data.remote.dto.auth.ForgotPasswordRequest
import com.agriedge.data.remote.dto.auth.LoginRequest
import com.agriedge.data.remote.dto.auth.RegisterRequest
import com.agriedge.data.remote.dto.auth.UpdateProfileRequest
import com.agriedge.data.remote.dto.auth.UserDto
import com.agriedge.domain.model.UserProfile
import com.agriedge.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    private var currentUser: UserProfile? = null

    override suspend fun getCurrentUser(): Result<UserProfile?> {
        return try {
            val accessToken = tokenManager.getAccessToken()
            if (accessToken == null || !tokenManager.isTokenValid(accessToken)) {
                currentUser = null
                return Result.success(null)
            }

            if (currentUser != null) {
                return Result.success(currentUser)
            }

            val userDto = authApiService.getProfile()
            currentUser = userDto.toDomainModel()
            Result.success(currentUser)
        } catch (e: Exception) {
            val error = NetworkErrorHandler.handleException(e)
            Result.failure(Exception(error.message))
        }
    }

    override suspend fun login(email: String, password: String): Result<UserProfile> {
        return try {
            val response = authApiService.login(
                LoginRequest(email = email.trim(), password = password)
            )
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            tokenManager.clearPendingOtpSession()
            tokenManager.clearPendingOtpContext()
            val user = response.user.toDomainModel()
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            val error = NetworkErrorHandler.handleException(e)
            Result.failure(Exception(error.message))
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        location: String
    ): Result<UserProfile> {
        return try {
            val locationParts = location.split(",").map { it.trim() }
            val state = locationParts.getOrNull(0).orEmpty()
            val district = locationParts.getOrNull(1).orEmpty()

            val response = authApiService.register(
                RegisterRequest(
                    email = email.trim(),
                    phoneNumber = phone,
                    name = name,
                    password = password,
                    state = state,
                    district = district,
                    preferredLanguage = "en"
                )
            )
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            val user = response.user.toDomainModel()
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            val error = NetworkErrorHandler.handleException(e)
            Result.failure(Exception(error.message))
        }
    }

    override suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = authApiService.forgotPassword(ForgotPasswordRequest(email.trim()))
            Result.success(response.message)
        } catch (e: Exception) {
            val error = NetworkErrorHandler.handleException(e)
            Result.failure(Exception(error.message))
        }
    }

    suspend fun refreshToken(): Result<Unit> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
                ?: return Result.failure(Exception("No refresh token available"))

            val response = authApiService.refreshToken(mapOf("refreshToken" to refreshToken))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearTokens()
            currentUser = null
            val error = NetworkErrorHandler.handleException(e)
            Result.failure(Exception(error.message))
        }
    }

    suspend fun getProfile(): Result<UserProfile> {
        return try {
            val userDto = authApiService.getProfile()
            val user = userDto.toDomainModel()
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            val error = NetworkErrorHandler.handleException(e)
            Result.failure(Exception(error.message))
        }
    }

    suspend fun updateProfile(
        name: String? = null,
        location: String? = null,
        preferredLanguage: String? = null
    ): Result<UserProfile> {
        return try {
            var state: String? = null
            var district: String? = null
            if (location != null) {
                val locationParts = location.split(",").map { it.trim() }
                state = locationParts.getOrNull(0)
                district = locationParts.getOrNull(1)
            }

            val userDto = authApiService.updateProfile(
                UpdateProfileRequest(
                    name = name,
                    state = state,
                    district = district,
                    preferredLanguage = preferredLanguage
                )
            )
            val user = userDto.toDomainModel()
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            val error = NetworkErrorHandler.handleException(e)
            Result.failure(Exception(error.message))
        }
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
        tokenManager.clearPendingOtpSession()
        tokenManager.clearPendingOtpContext()
        currentUser = null
    }

    private fun UserDto.toDomainModel(): UserProfile {
        val loc = listOf(state, district).filter { it.isNotBlank() }.joinToString(", ")
        return UserProfile(
            userId = userId,
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            location = loc,
            createdAt = registrationDate
        )
    }
}
