package com.thuna.assistant.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thuna.assistant.data.model.ChatMessage
import com.thuna.assistant.presentation.ui.components.VoiceInputButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val isModelReady by viewModel.isModelReady.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ==================== TOP APP BAR ====================
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Thuna",
                        fontSize = 24.sp,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status indicator dot
                        val statusColor = if (isModelReady) {
                            Color(0xFF4CAF50) // Green = Ready
                        } else {
                            Color(0xFFFF9800) // Orange = Loading
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(statusColor, shape = RoundedCornerShape(50))
                        )
                        Text(
                            text = if (isModelReady) "Ready • Offline" else "Loading AI...",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                        if (isListening) {
                            Text(
                                text = "🔴 Recording...",
                                fontSize = 14.sp,
                                color = Color(0xFFFF5722)
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0F4C5C) // Deep Teal
            )
        )
        
        // ==================== CHAT MESSAGES ====================
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = listState,
            reverseLayout = false
        ) {
            items(messages) { message ->
                ChatBubble(message = message)
            }
            
            // Typing indicator
            if (isProcessing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8EAF6)
                            ),
                            modifier = Modifier.width(60.dp)
                        ) {
                            Text(
                                text = "•••",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 20.sp,
                                color = Color(0xFF0F4C5C)
                            )
                        }
                    }
                }
            }
        }
        
        // ==================== INPUT AREA ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voice Input Button (NEW)
            VoiceInputButton(
                viewModel = viewModel,
                modifier = Modifier
            )
            
            // Text Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        text = if (isModelReady) "Ask Thuna..." else "Loading...",
                        fontSize = 18.sp
                    ) 
                },
                enabled = isModelReady,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0F4C5C),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
            )
            
            // Send Button
            FloatingActionButton(
                onClick = {
                    if (isModelReady && inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                containerColor = if (isModelReady && inputText.isNotBlank()) Color(0xFF0F4C5C) else Color.LightGray,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ==================== CHAT BUBBLE COMPONENT ====================
@Composable
fun ChatBubble(message: ChatMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isUser) 20.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF0F4C5C) else Color(0xFFE8EAF6)
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(14.dp),
                color = if (message.isUser) Color.White else Color(0xFF1A1A1A),
                fontSize = 18.sp,
                lineHeight = 26.sp
            )
        }
    }
}
