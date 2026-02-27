package com.agriedge.di

import androidx.activity.ComponentActivity
import com.agriedge.data.camera.CameraPermissionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object CameraModule {

    @Provides
    @ActivityScoped
    fun provideCameraPermissionHandler(
        activity: ComponentActivity
    ): CameraPermissionHandler {
        return CameraPermissionHandler(
            activity = activity,
            onPermissionGranted = { /* no-op for DI */ },
            onPermissionDenied = { /* no-op for DI */ }
        )
    }
}