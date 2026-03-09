package com.agriedge.di

import com.agriedge.data.local.storage.ImageStorageManager
import com.agriedge.data.ml.classifier.DiseaseClassifier
import com.agriedge.data.ml.classifier.GenericImageClassifier
import com.agriedge.data.ml.preprocessor.ImagePreprocessor
import com.agriedge.domain.repository.DiagnosisRepository
import com.agriedge.domain.repository.SyncQueueRepository
import com.agriedge.domain.repository.TreatmentRepository
import com.agriedge.domain.usecase.DiagnoseDiseaseUseCase
import com.agriedge.domain.usecase.GetDiagnosisHistoryUseCase
import com.agriedge.domain.usecase.GetTreatmentRecommendationUseCase
import com.agriedge.domain.usecase.GetTreatmentRecommendationUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing use case instances.
 * 
 * Use cases encapsulate business logic and coordinate between
 * repositories, data sources, and other components.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    /**
     * Provides DiagnoseDiseaseUseCase.
     */
    @Provides
    @Singleton
    fun provideDiagnoseDiseaseUseCase(
        imagePreprocessor: ImagePreprocessor,
        genericClassifier: GenericImageClassifier,
        diseaseClassifier: DiseaseClassifier,
        diagnosisRepository: DiagnosisRepository,
        imageStorageManager: ImageStorageManager,
        syncQueueRepository: SyncQueueRepository
    ): DiagnoseDiseaseUseCase {
        return DiagnoseDiseaseUseCase(
            imagePreprocessor = imagePreprocessor,
            genericClassifier = genericClassifier,
            diseaseClassifier = diseaseClassifier,
            diagnosisRepository = diagnosisRepository,
            imageStorageManager = imageStorageManager,
            syncQueueRepository = syncQueueRepository
        )
    }
    
    /**
     * Provides GetDiagnosisHistoryUseCase.
     */
    @Provides
    @Singleton
    fun provideGetDiagnosisHistoryUseCase(
        diagnosisRepository: DiagnosisRepository
    ): GetDiagnosisHistoryUseCase {
        return GetDiagnosisHistoryUseCase(diagnosisRepository)
    }
    
    /**
     * Provides GetTreatmentRecommendationUseCase.
     */
    @Provides
    @Singleton
    fun provideGetTreatmentRecommendationUseCase(
        treatmentRepository: TreatmentRepository
    ): GetTreatmentRecommendationUseCase {
        return GetTreatmentRecommendationUseCaseImpl(treatmentRepository)
    }
}
