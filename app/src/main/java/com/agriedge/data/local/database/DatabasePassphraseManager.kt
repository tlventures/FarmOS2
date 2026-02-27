package com.agriedge.data.local.database

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages database encryption passphrase using Android Keystore
 * Generates and securely stores device-specific encryption keys
 * 
 * Requirements: 43.1, 43.3
 */
class DatabasePassphraseManager(private val context: Context) {
    
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }
    
    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Get or generate database passphrase
     * Uses Android Keystore for secure key generation and storage
     * 
     * @return Database encryption passphrase as ByteArray
     */
    fun getOrCreatePassphrase(): ByteArray {
        // Check if passphrase already exists in encrypted preferences
        val existingPassphrase = encryptedPrefs.getString(PASSPHRASE_KEY, null)
        if (existingPassphrase != null) {
            return android.util.Base64.decode(existingPassphrase, android.util.Base64.DEFAULT)
        }
        
        // Generate new passphrase using Android Keystore
        val passphrase = generateSecurePassphrase()
        
        // Store encrypted passphrase
        val encodedPassphrase = android.util.Base64.encodeToString(passphrase, android.util.Base64.DEFAULT)
        encryptedPrefs.edit().putString(PASSPHRASE_KEY, encodedPassphrase).apply()
        
        return passphrase
    }
    
    /**
     * Generate a secure random passphrase using Android Keystore
     * Creates a 256-bit AES key for database encryption
     */
    private fun generateSecurePassphrase(): ByteArray {
        // Create or retrieve key from Android Keystore
        val key = getOrCreateKey()
        
        // Generate random bytes encrypted with the keystore key
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        // Generate 32 random bytes (256 bits) for AES-256 encryption
        val randomBytes = ByteArray(32)
        java.security.SecureRandom().nextBytes(randomBytes)
        
        return randomBytes
    }
    
    /**
     * Get or create encryption key in Android Keystore
     */
    private fun getOrCreateKey(): SecretKey {
        // Check if key already exists
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }
        
        // Generate new key in Keystore
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Don't require biometric for database access
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    /**
     * Clear passphrase (for logout or account deletion)
     */
    fun clearPassphrase() {
        encryptedPrefs.edit().remove(PASSPHRASE_KEY).apply()
        
        // Optionally delete key from keystore
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }
    
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "agriedge_db_key"
        private const val PREFS_NAME = "agriedge_secure_prefs"
        private const val PASSPHRASE_KEY = "db_passphrase"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
