package com.agriedge.di

import android.content.Context
import com.agriedge.data.local.database.AgriEdgeDatabase
import com.agriedge.data.local.database.DatabasePassphraseManager
import com.agriedge.data.local.database.dao.DiagnosisDao
import com.agriedge.data.local.database.dao.ProviderRatingDao
import com.agriedge.data.local.database.dao.SyncQueueDao
import com.agriedge.data.local.database.dao.TransactionDao
import com.agriedge.data.local.database.dao.TreatmentDao
import com.agriedge.data.local.database.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database dependencies
 * Requirements: 5.1, 43.1
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabasePassphraseManager(
        @ApplicationContext context: Context
    ): DatabasePassphraseManager {
        return DatabasePassphraseManager(context)
    }
    
    @Provides
    @Singleton
    fun provideAgriEdgeDatabase(
        @ApplicationContext context: Context,
        passphraseManager: DatabasePassphraseManager
    ): AgriEdgeDatabase {
        val passphrase = passphraseManager.getOrCreatePassphrase()
        return AgriEdgeDatabase.getInstance(context, passphrase)
    }
    
    @Provides
    @Singleton
    fun provideDiagnosisDao(database: AgriEdgeDatabase): DiagnosisDao {
        return database.diagnosisDao()
    }
    
    @Provides
    @Singleton
    fun provideUserProfileDao(database: AgriEdgeDatabase): UserProfileDao {
        return database.userProfileDao()
    }
    
    @Provides
    @Singleton
    fun provideTreatmentDao(database: AgriEdgeDatabase): TreatmentDao {
        return database.treatmentDao()
    }
    
    @Provides
    @Singleton
    fun provideTransactionDao(database: AgriEdgeDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    @Singleton
    fun provideProviderRatingDao(database: AgriEdgeDatabase): ProviderRatingDao {
        return database.ratingDao()
    }
    
    @Provides
    @Singleton
    fun provideSyncQueueDao(database: AgriEdgeDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }
}
