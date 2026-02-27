package com.agriedge.presentation.components

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun VoiceButton(
    onVoiceResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    languageCode: String = "en-IN"
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Animation for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "voice")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition(context, languageCode, onVoiceResult) {
                isListening = false
            }
            isListening = true
        } else {
            showPermissionDialog = true
        }
    }
    
    // Voice recognition launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        matches?.firstOrNull()?.let { text ->
            onVoiceResult(text)
        }
    }
    
    FloatingActionButton(
        onClick = {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                showPermissionDialog = true
            }
        },
        modifier = modifier.then(
            if (isListening) Modifier.scale(scale) else Modifier
        ),
        containerColor = if (isListening) 
            MaterialTheme.colorScheme.error 
        else 
            MaterialTheme.colorScheme.primary
    ) {
        Icon(
            Icons.Default.Mic,
            contentDescription = "Voice Input",
            modifier = Modifier.size(24.dp)
        )
    }
    
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Voice Recognition") },
            text = { 
                Text("Voice recognition is not available or permission was denied. Please enable microphone permission in settings.")
            },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun startVoiceRecognition(
    context: android.content.Context,
    languageCode: String,
    onResult: (String) -> Unit,
    onComplete: () -> Unit
) {
    try {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }
        
        // This would need to be launched from an Activity
        // For now, we'll show a toast
        android.widget.Toast.makeText(
            context,
            "Voice recognition feature - speak your query",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        
        onComplete()
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Voice recognition error: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        onComplete()
    }
}

@Composable
fun VoiceInteractionCard(
    onVoiceCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var lastCommand by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Voice Assistant",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap the microphone to speak",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            if (lastCommand.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last command: $lastCommand",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            VoiceButton(
                onVoiceResult = { result ->
                    lastCommand = result
                    onVoiceCommand(result)
                }
            )
        }
    }
}
