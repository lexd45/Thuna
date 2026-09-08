package com.thuna.assistant.presentation.ui.components

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.thuna.assistant.presentation.ui.ChatViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceInputButton(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isModelReady by viewModel.isModelReady.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    
    val recordPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )
    
    var isRecording by remember { mutableStateOf(false) }
    
    val enabled = isModelReady && !isProcessing

    IconButton(
        onClick = {
            if (!recordPermissionState.status.isGranted) {
                recordPermissionState.launchPermissionRequest()
                return@IconButton
            }
            
            // Voice is temporarily disabled – show message
            if (!viewModel.isVoskReady()) {
                Toast.makeText(
                    context,
                    "🔇 Voice input is currently disabled. Please type your message.",
                    Toast.LENGTH_SHORT
                ).show()
                return@IconButton
            }
            
            if (isListening) {
                viewModel.cancelListening()
                isRecording = false
            } else {
                viewModel.startListening()
                isRecording = true
                // Simulate voice input (since Vosk is disabled)
                // In a real implementation, this would record audio
            }
        },
        enabled = enabled,
        modifier = modifier.size(56.dp)
    ) {
        val icon = if (isListening) {
            Icons.Default.MicOff
        } else {
            Icons.Default.Mic
        }
        val tint = if (isListening) {
            MaterialTheme.colorScheme.error
        } else if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        }
        Icon(
            imageVector = icon,
            contentDescription = if (isListening) "Stop recording" else "Start voice input",
            tint = tint
        )
    }
}
