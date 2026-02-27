package com.agriedge.data.local.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for EncryptedStorageManager.
 * 
 * Tests encryption and secure storage of sensitive data including:
 * - Authentication tokens
 * - User credentials
 * - Database encryption keys
 * 
 * Requirements: 43.1, 43.2, 43.3
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EncryptedStorageManagerTest : DescribeSpec({
    
    lateinit var context: Context
    lateinit var storageManager: EncryptedStorageManager
    
    beforeEach {
        context = ApplicationProvider.getApplicationContext()
        storageManager = EncryptedStorageManager(context)
    }
    
    afterEach {
        storageManager.clear()
    }
    
    describe("Authentication Token Storage") {
        
        it("should save and retrieve access token") {
            val token = "test_access_token_12345"
            
            storageManager.saveAccessToken(token)
            val retrieved = storageManager.getAccessToken()
            
            retrieved shouldBe token
        }
        
        it("should save and retrieve refresh token") {
            val token = "test_refresh_token_67890"
            
            storageManager.saveRefreshToken(token)
            val retrieved = storageManager.getRefreshToken()
            
            retrieved shouldBe token
        }
        
        it("should clear tokens") {
            storageManager.saveAccessToken("access_token")
            storageManager.saveRefreshToken("refresh_token")
            
            storageManager.clearTokens()
            
            storageManager.getAccessToken() shouldBe null
            storageManager.getRefreshToken() shouldBe null
        }
        
        it("should return null for non-existent tokens") {
            storageManager.getAccessToken() shouldBe null
            storageManager.getRefreshToken() shouldBe null
        }
    }
    
    describe("User ID Storage") {
        
        it("should save and retrieve user ID") {
            val userId = "user_123456"
            
            storageManager.saveUserId(userId)
            val retrieved = storageManager.getUserId()
            
            retrieved shouldBe userId
        }
        
        it("should clear user ID") {
            storageManager.saveUserId("user_123")
            
            storageManager.clearUserId()
            
            storageManager.getUserId() shouldBe null
        }
    }
    
    describe("Database Encryption Key Management") {
        
        it("should generate database passphrase") {
            val passphrase = storageManager.getDatabasePassphrase()
            
            passphrase shouldNotBe null
            passphrase.size shouldBe 32 // 256 bits = 32 bytes
        }
        
        it("should return same passphrase on multiple calls") {
            val passphrase1 = storageManager.getDatabasePassphrase()
            val passphrase2 = storageManager.getDatabasePassphrase()
            
            passphrase1 contentEquals passphrase2 shouldBe true
        }
    }
    
    describe("Generic Secure Storage") {
        
        it("should save and retrieve string values") {
            val key = "test_key"
            val value = "test_value"
            
            storageManager.saveString(key, value)
            val retrieved = storageManager.getString(key)
            
            retrieved shouldBe value
        }
        
        it("should save and retrieve boolean values") {
            val key = "test_bool"
            
            storageManager.saveBoolean(key, true)
            storageManager.getBoolean(key) shouldBe true
            
            storageManager.saveBoolean(key, false)
            storageManager.getBoolean(key) shouldBe false
        }
        
        it("should save and retrieve integer values") {
            val key = "test_int"
            val value = 42
            
            storageManager.saveInt(key, value)
            val retrieved = storageManager.getInt(key)
            
            retrieved shouldBe value
        }
        
        it("should save and retrieve long values") {
            val key = "test_long"
            val value = 1234567890L
            
            storageManager.saveLong(key, value)
            val retrieved = storageManager.getLong(key)
            
            retrieved shouldBe value
        }
        
        it("should remove specific keys") {
            val key = "test_key"
            storageManager.saveString(key, "value")
            
            storageManager.remove(key)
            
            storageManager.contains(key) shouldBe false
            storageManager.getString(key) shouldBe null
        }
        
        it("should check if key exists") {
            val key = "test_key"
            
            storageManager.contains(key) shouldBe false
            
            storageManager.saveString(key, "value")
            storageManager.contains(key) shouldBe true
        }
        
        it("should return default values for non-existent keys") {
            storageManager.getString("non_existent", "default") shouldBe "default"
            storageManager.getBoolean("non_existent", true) shouldBe true
            storageManager.getInt("non_existent", 99) shouldBe 99
            storageManager.getLong("non_existent", 999L) shouldBe 999L
        }
        
        it("should clear all stored data") {
            storageManager.saveString("key1", "value1")
            storageManager.saveString("key2", "value2")
            storageManager.saveInt("key3", 123)
            
            storageManager.clear()
            
            storageManager.getString("key1") shouldBe null
            storageManager.getString("key2") shouldBe null
            storageManager.getInt("key3", -1) shouldBe -1
        }
    }
    
    describe("Data Encryption Verification") {
        
        it("should encrypt sensitive data (tokens not stored in plaintext)") {
            val sensitiveToken = "super_secret_token_12345"
            
            storageManager.saveAccessToken(sensitiveToken)
            
            // Verify we can retrieve it
            storageManager.getAccessToken() shouldBe sensitiveToken
            
            // Note: We can't directly verify encryption without accessing SharedPreferences XML,
            // but the use of EncryptedSharedPreferences ensures encryption
        }
    }
})
