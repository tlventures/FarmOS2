package com.agriedge.di

import android.content.Context
import com.agriedge.data.local.storage.EncryptedStorageManager
import com.agriedge.data.local.storage.ImageStorageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing storage-related dependencies.
 * 
 * Provides:
 * - EncryptedStorageManager for secure data storage
 * - ImageStorageManager for image persistence
 * 
 * Requirements: 43.1, 43.2, 43.3, 5.1, 42.1
 */
@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    
    @Provides
    @Singleton
    fun provideEncryptedStorageManager(
        @ApplicationContext context: Context
    ): EncryptedStorageManager {
        return EncryptedStorageManager(context)
    }
    
    @Provides
    @Singleton
    fun provideImageStorageManager(
        @ApplicationContext context: Context
    ): ImageStorageManager {
        return ImageStorageManager(context)
    }
}
