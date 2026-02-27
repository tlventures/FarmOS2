package com.agriedge.di

import android.content.Context
import com.agriedge.data.ml.classifier.DiseaseClassifier
import com.agriedge.data.ml.preprocessor.ImagePreprocessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for ML-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object MLModule {
    
    @Provides
    @Singleton
    fun provideDiseaseClassifier(
        @ApplicationContext context: Context
    ): DiseaseClassifier {
        return DiseaseClassifier(context)
    }
    
    @Provides
    @Singleton
    fun provideImagePreprocessor(): ImagePreprocessor {
        return ImagePreprocessor()
    }
}
