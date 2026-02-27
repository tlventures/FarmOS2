package com.agriedge.link

import android.app.Application
import com.agriedge.data.ml.classifier.DiseaseClassifier
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for AgriEdge-Link.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class AgriEdgeApplication : Application() {
    
    @Inject
    lateinit var diseaseClassifier: DiseaseClassifier
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ML classifier in background
        applicationScope.launch {
            try {
                diseaseClassifier.initialize()
            } catch (e: Exception) {
                // Log error but don't crash app
                android.util.Log.e("AgriEdgeApp", "Failed to initialize classifier", e)
            }
        }
    }
}
