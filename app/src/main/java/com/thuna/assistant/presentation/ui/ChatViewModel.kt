package com.thuna.assistant.presentation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thuna.assistant.data.model.ChatMessage
import com.thuna.assistant.data.repository.AssistantRepository
import com.thuna.assistant.domain.model.IntentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: AssistantRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _isModelReady = MutableStateFlow(false)
    val isModelReady: StateFlow<Boolean> = _isModelReady.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val TAG = "ThunaVM"

    fun isVoskReady(): Boolean = repository.isVoskReady()

    init {
        _messages.value = listOf(
            ChatMessage(
                content = "⏳ Loading AI model... This may take 30-60 seconds.",
                isUser = false
            )
        )

        viewModelScope.launch {
            _isModelReady.value = false
            
            // Load LLM
            val llmResult = withTimeoutOrNull(120000) {
                repository.initializeLLM()
            }
            
            val llmOk = llmResult?.isSuccess == true
            _isModelReady.value = true
            
            val statusMessage = buildString {
                appendLine("✅ Thuna is ready!")
                if (llmOk) appendLine("✅ AI Brain: Active (Gemma 2B)")
                else appendLine("⚠️ AI Brain: Fallback mode (Rule Engine only)")
                appendLine("⚠️ Voice Recognition: Disabled (Safe Mode)")
                appendLine("\n💡 Try: 'Give me a health tip'")
                appendLine("\n🛡️ Remember: I'm not a doctor. Always consult a professional.")
            }
            
            _messages.value = listOf(
                ChatMessage(content = statusMessage, isUser = false)
            )
            
            Log.d(TAG, "LLM OK: $llmOk")
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        addMessage(ChatMessage(content = text, isUser = true))
        
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                repository.processCommand(text).collect { result ->
                    when (result) {
                        is IntentResult.MedicationReminder -> {
                            val response = """
                                ✅ I'll remind you to take ${result.medicine} in the ${result.time}.
                                Would you like me to set a daily reminder?
                            """.trimIndent()
                            addMessage(ChatMessage(content = response, isUser = false))
                        }
                        is IntentResult.HealthTip -> {
                            addMessage(ChatMessage(content = "💡 Health Tip: ${result.tip}", isUser = false))
                        }
                        is IntentResult.Alarm -> {
                            addMessage(ChatMessage(content = "⏰ Alarm set for ${result.time}. I'll remind you!", isUser = false))
                        }
                        is IntentResult.GeneralResponse -> {
                            addMessage(ChatMessage(content = result.response, isUser = false))
                        }
                        IntentResult.Unknown -> {
                            addMessage(
                                ChatMessage(
                                    content = "I'm not sure I understood. Could you please say that differently?",
                                    isUser = false
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                addMessage(
                    ChatMessage(
                        content = "Sorry, I encountered an error. Please try again.",
                        isUser = false
                    )
                )
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // Called when user taps the mic button
    fun startListening() {
        _isListening.value = true
        addMessage(ChatMessage(content = "🎤 Listening... Speak now!", isUser = false))
    }

    // Called when recording stops with audio data
    fun processVoiceInput(audioData: ByteArray) {
        _isListening.value = false
        _isProcessing.value = true
        
        viewModelScope.launch {
            try {
                val transcribedText = repository.transcribeAudio(audioData)
                if (!transcribedText.isNullOrEmpty()) {
                    // User message is the transcribed text
                    addMessage(ChatMessage(content = "🎤 $transcribedText", isUser = true))
                    
                    // Process the transcribed text as a command
                    repository.processCommand(transcribedText).collect { result ->
                        when (result) {
                            is IntentResult.MedicationReminder -> {
                                val response = """
                                    ✅ I'll remind you to take ${result.medicine} in the ${result.time}.
                                    Would you like me to set a daily reminder?
                                """.trimIndent()
                                addMessage(ChatMessage(content = response, isUser = false))
                            }
                            is IntentResult.HealthTip -> {
                                addMessage(ChatMessage(content = "💡 Health Tip: ${result.tip}", isUser = false))
                            }
                            is IntentResult.Alarm -> {
                                addMessage(ChatMessage(content = "⏰ Alarm set for ${result.time}. I'll remind you!", isUser = false))
                            }
                            is IntentResult.GeneralResponse -> {
                                addMessage(ChatMessage(content = result.response, isUser = false))
                            }
                            IntentResult.Unknown -> {
                                addMessage(
                                    ChatMessage(
                                        content = "I'm not sure I understood. Could you please say that differently?",
                                        isUser = false
                                    )
                                )
                            }
                        }
                    }
                } else {
                    addMessage(
                        ChatMessage(
                            content = "Voice recognition is currently disabled in Safe Mode.",
                            isUser = false
                        )
                    )
                }
            } catch (e: Exception) {
                addMessage(
                    ChatMessage(
                        content = "Voice processing failed. Please try typing your message.",
                        isUser = false
                    )
                )
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun cancelListening() {
        _isListening.value = false
        addMessage(ChatMessage(content = "❌ Listening cancelled.", isUser = false))
    }

    private fun addMessage(message: ChatMessage) {
        _messages.update { it + message }
    }

    fun clearMessages() {
        _messages.value = emptyList()
        addMessage(
            ChatMessage(
                content = "👋 Vanakkam! I'm Thuna, your health companion. How can I help you today?",
                isUser = false
            )
        )
    }
}
