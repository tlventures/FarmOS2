package com.agriedge.data.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Handles camera permission requests and status checks.
 * Provides rationale for camera access and handles permission denied scenarios.
 * 
 * Requirements: 1.1
 */
class CameraPermissionHandler(
    private val activity: ComponentActivity,
    private val onPermissionGranted: () -> Unit,
    private val onPermissionDenied: (shouldShowRationale: Boolean) -> Unit
) {
    
    companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        
        /**
         * Checks if camera permission is granted.
         */
        fun hasPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                CAMERA_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private val permissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                val shouldShowRationale = activity.shouldShowRequestPermissionRationale(CAMERA_PERMISSION)
                onPermissionDenied(shouldShowRationale)
            }
        }
    
    /**
     * Requests camera permission.
     * If permission is already granted, invokes onPermissionGranted immediately.
     */
    fun requestPermission() {
        when {
            hasPermission(activity) -> {
                // Permission already granted
                onPermissionGranted()
            }
            else -> {
                // Request permission
                permissionLauncher.launch(CAMERA_PERMISSION)
            }
        }
    }
    
    /**
     * Checks if we should show permission rationale.
     * Returns true if the user has previously denied the permission.
     */
    fun shouldShowRationale(): Boolean {
        return activity.shouldShowRequestPermissionRationale(CAMERA_PERMISSION)
    }
    
    /**
     * Gets the rationale message for camera permission.
     */
    fun getRationaleMessage(): String {
        return "Camera access is required to capture images of crop leaves for disease diagnosis. " +
               "The app needs to take photos of affected leaves to analyze them using AI and provide " +
               "accurate disease identification and treatment recommendations."
    }
    
    /**
     * Gets the message to show when permission is permanently denied.
     */
    fun getPermanentlyDeniedMessage(): String {
        return "Camera permission is required for this app to function. " +
               "Please enable camera access in your device settings to use the disease diagnosis feature."
    }
}

/**
 * Permission state for UI representation.
 */
sealed class PermissionState {
    /**
     * Permission is granted.
     */
    object Granted : PermissionState()
    
    /**
     * Permission is denied but can be requested again.
     * User should see rationale before requesting.
     */
    object DeniedWithRationale : PermissionState()
    
    /**
     * Permission is permanently denied.
     * User must enable it in settings.
     */
    object PermanentlyDenied : PermissionState()
    
    /**
     * Permission status is unknown (not yet requested).
     */
    object Unknown : PermissionState()
}

/**
 * Helper function to get current permission state.
 */
fun ComponentActivity.getCameraPermissionState(): PermissionState {
    return when {
        CameraPermissionHandler.hasPermission(this) -> {
            PermissionState.Granted
        }
        shouldShowRequestPermissionRationale(CameraPermissionHandler.CAMERA_PERMISSION) -> {
            PermissionState.DeniedWithRationale
        }
        else -> {
            // Could be either Unknown or PermanentlyDenied
            // We can't distinguish between these two states without additional tracking
            PermissionState.Unknown
        }
    }
}
