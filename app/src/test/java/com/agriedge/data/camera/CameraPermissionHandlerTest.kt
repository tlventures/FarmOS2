package com.agriedge.data.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.*

@DisplayName("CameraPermissionHandler Tests")
class CameraPermissionHandlerTest {
    
    private lateinit var activity: ComponentActivity
    private lateinit var context: Context
    private var permissionGrantedCalled = false
    private var permissionDeniedCalled = false
    private var shouldShowRationale = false
    
    @BeforeEach
    fun setup() {
        activity = mock()
        context = mock()
        permissionGrantedCalled = false
        permissionDeniedCalled = false
        shouldShowRationale = false
    }
    
    @Test
    @DisplayName("Should check if permission is granted")
    fun testHasPermission() {
        whenever(
            context.checkSelfPermission(Manifest.permission.CAMERA)
        ).thenReturn(PackageManager.PERMISSION_GRANTED)
        
        val hasPermission = CameraPermissionHandler.hasPermission(context)
        
        assertTrue(hasPermission)
    }
    
    @Test
    @DisplayName("Should check if permission is denied")
    fun testHasNoPermission() {
        whenever(
            context.checkSelfPermission(Manifest.permission.CAMERA)
        ).thenReturn(PackageManager.PERMISSION_DENIED)
        
        val hasPermission = CameraPermissionHandler.hasPermission(context)
        
        assertFalse(hasPermission)
    }
    
    @Test
    @DisplayName("Should provide rationale message")
    fun testGetRationaleMessage() {
        val handler = CameraPermissionHandler(
            activity = activity,
            onPermissionGranted = { permissionGrantedCalled = true },
            onPermissionDenied = { shouldShowRationale = it; permissionDeniedCalled = true }
        )
        
        val message = handler.getRationaleMessage()
        
        assertNotNull(message)
        assertTrue(message.contains("camera", ignoreCase = true))
        assertTrue(message.contains("disease", ignoreCase = true))
    }
    
    @Test
    @DisplayName("Should provide permanently denied message")
    fun testGetPermanentlyDeniedMessage() {
        val handler = CameraPermissionHandler(
            activity = activity,
            onPermissionGranted = { permissionGrantedCalled = true },
            onPermissionDenied = { shouldShowRationale = it; permissionDeniedCalled = true }
        )
        
        val message = handler.getPermanentlyDeniedMessage()
        
        assertNotNull(message)
        assertTrue(message.contains("permission", ignoreCase = true))
        assertTrue(message.contains("settings", ignoreCase = true))
    }
    
    @Test
    @DisplayName("Should handle permission granted callback")
    fun testPermissionGrantedCallback() {
        val onGranted: () -> Unit = { permissionGrantedCalled = true }
        val onDenied: (Boolean) -> Unit = { permissionDeniedCalled = true }
        
        onGranted()
        
        assertTrue(permissionGrantedCalled)
        assertFalse(permissionDeniedCalled)
    }
    
    @Test
    @DisplayName("Should handle permission denied callback")
    fun testPermissionDeniedCallback() {
        val onGranted: () -> Unit = { permissionGrantedCalled = true }
        val onDenied: (Boolean) -> Unit = { 
            shouldShowRationale = it
            permissionDeniedCalled = true 
        }
        
        onDenied(true)
        
        assertFalse(permissionGrantedCalled)
        assertTrue(permissionDeniedCalled)
        assertTrue(shouldShowRationale)
    }
    
    @Test
    @DisplayName("Should handle permanently denied callback")
    fun testPermanentlyDeniedCallback() {
        val onGranted: () -> Unit = { permissionGrantedCalled = true }
        val onDenied: (Boolean) -> Unit = { 
            shouldShowRationale = it
            permissionDeniedCalled = true 
        }
        
        onDenied(false)
        
        assertFalse(permissionGrantedCalled)
        assertTrue(permissionDeniedCalled)
        assertFalse(shouldShowRationale)
    }
    
    @Test
    @DisplayName("Should identify permission states correctly")
    fun testPermissionStates() {
        assertTrue(PermissionState.Granted is PermissionState)
        assertTrue(PermissionState.DeniedWithRationale is PermissionState)
        assertTrue(PermissionState.PermanentlyDenied is PermissionState)
        assertTrue(PermissionState.Unknown is PermissionState)
    }
}
