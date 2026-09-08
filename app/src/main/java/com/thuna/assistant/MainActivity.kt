package com.thuna.assistant

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.thuna.assistant.presentation.theme.ThunaTheme
import com.thuna.assistant.presentation.ui.ChatScreen
import com.thuna.assistant.presentation.ui.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ThunaTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val recordAudioPermissionState = rememberPermissionState(
                        Manifest.permission.RECORD_AUDIO
                    )
                    
                    // Request microphone permission on launch
                    LaunchedEffect(Unit) {
                        if (recordAudioPermissionState.status != PermissionStatus.Granted) {
                            recordAudioPermissionState.launchPermissionRequest()
                        }
                    }
                    
                    val viewModel: ChatViewModel = viewModel()
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}
