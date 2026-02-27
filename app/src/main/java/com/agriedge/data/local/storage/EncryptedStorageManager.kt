package com.agriedge.data.local.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages encrypted storage for sensitive data using Android Keystore and EncryptedSharedPreferences.
 * 
 * This class provides secure storage for:
 * - Authentication tokens (JWT access and refresh tokens)
 * - User credentials
 * - Database encryption keys
 * - Other sensitive application data
 * 
 * All data is encrypted using AES-256 encryption with keys stored in Android Keystore,
 * which provides hardware-backed security on supported devices.
 * 
 * Requirements: 43.1, 43.2, 43.3
 */
@Singleton
class EncryptedStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    // Authentication token storage
    
    fun saveAccessToken(token: String) {
        encryptedPrefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }
    
    fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun saveRefreshToken(token: String) {
        encryptedPrefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }
    
    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }
    
    // User ID storage
    
    fun saveUserId(userId: String) {
        encryptedPrefs.edit().putString(KEY_USER_ID, userId).apply()
    }
    
    fun getUserId(): String? {
        return encryptedPrefs.getString(KEY_USER_ID, null)
    }
    
    fun clearUserId() {
        encryptedPrefs.edit().remove(KEY_USER_ID).apply()
    }
    
    // Database encryption key management
    
    /**
     * Generates or retrieves the database encryption key from Android Keystore.
     * The key is generated once and stored securely in the hardware-backed keystore.
     * 
     * @return ByteArray containing the database encryption passphrase
     */
    fun getDatabasePassphrase(): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        if (!keyStore.containsAlias(DB_KEY_ALIAS)) {
            generateDatabaseKey()
        }
        
        return retrieveDatabaseKey()
    }
    
    private fun generateDatabaseKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            DB_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    private fun retrieveDatabaseKey(): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        val secretKey = keyStore.getKey(DB_KEY_ALIAS, null) as SecretKey
        return secretKey.encoded
    }
    
    // Generic secure storage methods
    
    fun saveString(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }
    
    fun getString(key: String, defaultValue: String? = null): String? {
        return encryptedPrefs.getString(key, defaultValue)
    }
    
    fun saveBoolean(key: String, value: Boolean) {
        encryptedPrefs.edit().putBoolean(key, value).apply()
    }
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return encryptedPrefs.getBoolean(key, defaultValue)
    }
    
    fun saveInt(key: String, value: Int) {
        encryptedPrefs.edit().putInt(key, value).apply()
    }
    
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return encryptedPrefs.getInt(key, defaultValue)
    }
    
    fun saveLong(key: String, value: Long) {
        encryptedPrefs.edit().putLong(key, value).apply()
    }
    
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return encryptedPrefs.getLong(key, defaultValue)
    }
    
    fun remove(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }
    
    fun clear() {
        encryptedPrefs.edit().clear().apply()
    }
    
    fun contains(key: String): Boolean {
        return encryptedPrefs.contains(key)
    }
    
    companion object {
        private const val PREFS_NAME = "agriedge_secure_prefs"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val DB_KEY_ALIAS = "agriedge_db_key"
        
        // Predefined keys
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
    }
}
