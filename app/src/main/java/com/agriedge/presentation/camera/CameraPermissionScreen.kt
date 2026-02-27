package com.agriedge.presentation.camera

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agriedge.data.camera.CameraPermissionHandler
import com.agriedge.data.camera.PermissionState
import com.agriedge.data.camera.getCameraPermissionState

/**
 * Composable screen for handling camera permission requests.
 * Displays appropriate UI based on permission state and provides rationale.
 * 
 * Requirements: 1.1
 */
@Composable
fun CameraPermissionScreen(
    onPermissionGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
        ?: throw IllegalStateException("CameraPermissionScreen must be used within a ComponentActivity")
    
    var permissionState by remember { mutableStateOf(activity.getCameraPermissionState()) }
    
    val permissionHandler = remember {
        CameraPermissionHandler(
            activity = activity,
            onPermissionGranted = {
                permissionState = PermissionState.Granted
                onPermissionGranted()
            },
            onPermissionDenied = { shouldShowRationale ->
                permissionState = if (shouldShowRationale) {
                    PermissionState.DeniedWithRationale
                } else {
                    PermissionState.PermanentlyDenied
                }
            }
        )
    }
    
    // Check permission state on composition
    LaunchedEffect(Unit) {
        if (CameraPermissionHandler.hasPermission(context)) {
            onPermissionGranted()
        }
    }
    
    when (permissionState) {
        PermissionState.Granted -> {
            // Permission granted, proceed
            LaunchedEffect(Unit) {
                onPermissionGranted()
            }
        }
        
        PermissionState.Unknown,
        PermissionState.DeniedWithRationale -> {
            PermissionRationaleContent(
                permissionHandler = permissionHandler,
                onRequestPermission = {
                    permissionHandler.requestPermission()
                },
                modifier = modifier
            )
        }
        
        PermissionState.PermanentlyDenied -> {
            PermanentlyDeniedContent(
                permissionHandler = permissionHandler,
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun PermissionRationaleContent(
    permissionHandler: CameraPermissionHandler,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Camera",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Camera Access Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = permissionHandler.getRationaleMessage(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Camera Permission")
        }
    }
}

@Composable
private fun PermanentlyDeniedContent(
    permissionHandler: CameraPermissionHandler,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Camera",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Camera Permission Denied",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = permissionHandler.getPermanentlyDeniedMessage(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Settings")
        }
    }
}
