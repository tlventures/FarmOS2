package com.agriedge.di

import com.agriedge.data.local.database.dao.DiagnosisDao
import com.agriedge.data.local.database.dao.SyncQueueDao
import com.agriedge.data.local.database.dao.TransactionDao
import com.agriedge.data.local.database.dao.TreatmentDao
import com.agriedge.data.repository.AuthRepositoryImpl
import com.agriedge.data.repository.ColdStorageRepositoryImpl
import com.agriedge.data.repository.DiagnosisRepositoryImpl
import com.agriedge.data.repository.EquipmentRentalRepositoryImpl
import com.agriedge.data.repository.MarketRepositoryImpl
import com.agriedge.data.repository.SyncQueueRepositoryImpl
import com.agriedge.data.repository.TransactionRepositoryImpl
import com.agriedge.data.repository.TreatmentRepositoryImpl
import com.agriedge.domain.repository.AuthRepository
import com.agriedge.domain.repository.ColdStorageRepository
import com.agriedge.domain.repository.DiagnosisRepository
import com.agriedge.domain.repository.EquipmentRentalRepository
import com.agriedge.domain.repository.MarketRepository
import com.agriedge.domain.repository.SyncQueueRepository
import com.agriedge.domain.repository.TransactionRepository
import com.agriedge.domain.repository.TreatmentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 * 
 * This module binds repository interfaces to their implementations
 * for dependency injection throughout the app.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * Provides DiagnosisRepository implementation.
     */
    @Provides
    @Singleton
    fun provideDiagnosisRepository(
        diagnosisDao: DiagnosisDao
    ): DiagnosisRepository {
        return DiagnosisRepositoryImpl(diagnosisDao)
    }
    
    /**
     * Provides SyncQueueRepository implementation.
     */
    @Provides
    @Singleton
    fun provideSyncQueueRepository(
        syncQueueDao: SyncQueueDao
    ): SyncQueueRepository {
        return SyncQueueRepositoryImpl(syncQueueDao)
    }
    
    /**
     * Provides TreatmentRepository implementation.
     */
    @Provides
    @Singleton
    fun provideTreatmentRepository(
        treatmentDao: TreatmentDao
    ): TreatmentRepository {
        return TreatmentRepositoryImpl(treatmentDao)
    }
    
    /**
     * Provides MarketRepository implementation.
     */
    @Provides
    @Singleton
    fun provideMarketRepository(): MarketRepository {
        return MarketRepositoryImpl()
    }
    
    /**
     * Provides TransactionRepository implementation.
     */
    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao
    ): TransactionRepository {
        return TransactionRepositoryImpl(transactionDao)
    }
    
    /**
     * Provides ColdStorageRepository implementation.
     */
    @Provides
    @Singleton
    fun provideColdStorageRepository(): ColdStorageRepository {
        return ColdStorageRepositoryImpl()
    }
    
    /**
     * Provides EquipmentRentalRepository implementation.
     */
    @Provides
    @Singleton
    fun provideEquipmentRentalRepository(): EquipmentRentalRepository {
        return EquipmentRentalRepositoryImpl()
    }
    
    /**
     * Provides AuthRepository implementation.
     */
    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }
}
