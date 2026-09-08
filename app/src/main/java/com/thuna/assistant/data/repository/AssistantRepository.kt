package com.thuna.assistant.data.repository

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.thuna.assistant.R
import com.thuna.assistant.domain.model.IntentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class AssistantRepository(private val context: Context) {

    private var llmInference: LlmInference? = null
    private val TAG = "ThunaRepo"

    private val SYSTEM_PROMPT = """
        You are Thuna, a kind, patient, and caring health assistant for elderly users in India.
        Your name means "support" in Tamil.
        
        RULES:
        1. Always speak in simple, clear language. Use short sentences.
        2. Be warm, respectful, and encouraging.
        3. Provide helpful health information and general wellness tips.
        4. NEVER give a medical diagnosis. Always say: "I am not a doctor. Please consult a healthcare professional for serious concerns."
        5. If the user mentions symptoms, suggest rest, hydration, and checking with a doctor if it persists.
        6. For medication questions, remind them to follow their doctor's prescription.
        7. Be patient and repeat information if needed.
        8. Respond in Hinglish or English as the user prefers.
        
        Remember: You are a supportive companion, not a doctor.
    """.trimIndent()

    suspend fun initializeLLM(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting model initialization...")
            val modelFile = File(context.filesDir, "gemma_2b_it_gpu_int4.task")
            
            if (!modelFile.exists()) {
                Log.d(TAG, "Model not found. Copying from raw resources...")
                try {
                    val inputStream = context.resources.openRawResource(R.raw.gemma_2b_it_gpu_int4)
                    modelFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                    Log.d(TAG, "Model copied. Size: ${modelFile.length() / (1024 * 1024)} MB")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to copy model", e)
                    return@withContext Result.failure(Exception("Model file not found in res/raw/"))
                }
            }

            if (modelFile.length() < 100 * 1024 * 1024) {
                Log.e(TAG, "Model file too small")
                return@withContext Result.failure(Exception("Model corrupted"))
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(512)
                .setTemperature(0.7f)
                .setTopK(40)
                .build()
            
            llmInference = LlmInference.createFromOptions(context, options)
            Log.d(TAG, "LLM initialized on CPU!")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "LLM init failed", e)
            Result.failure(e)
        }
    }

    // ==================== SPEECH-TO-TEXT (SAFE MODE) ====================
    
    suspend fun transcribeAudio(audioData: ByteArray): String? {
        Log.w(TAG, "Voice recognition is disabled in this version.")
        return null
    }

    fun isVoskReady(): Boolean = false

    // ==================== INTELLIGENCE (ENHANCED FALLBACK) ====================

    fun processCommand(input: String): Flow<IntentResult> = flow {
        // STEP 1: Rule Engine (Handles exact commands)
        val ruleResult = handleRuleBased(input)
        if (ruleResult != null) {
            emit(ruleResult)
            return@flow
        }
        
        // STEP 2: LLM (if available)
        val llm = llmInference
        if (llm != null) {
            try {
                val prompt = """
                    $SYSTEM_PROMPT
                    
                    User: $input
                    Thuna:
                """.trimIndent()
                
                val response = llm.generateResponse(prompt)
                if (!response.isNullOrEmpty()) {
                    emit(IntentResult.GeneralResponse(response))
                    return@flow
                }
            } catch (e: Exception) {
                Log.e(TAG, "LLM failed", e)
            }
        }
        
        // STEP 3: Enhanced Fallback (Covers 15+ queries)
        val fallbackResponse = generateFallbackResponse(input)
        emit(IntentResult.GeneralResponse(fallbackResponse))
    }

    // ==================== ENHANCED FALLBACK SYSTEM ====================
    
    private fun generateFallbackResponse(input: String): String {
        val lower = input.lowercase().trim()
        
        // --- Greetings ---
        if (lower.matches(Regex(".*(hello|hi|hey|namaste|vanakkam|good morning|good afternoon|good evening).*"))) {
            return "Vanakkam! 🙏 I'm Thuna, your health companion. How can I help you today?"
        }
        
        // --- How are you? ---
        if (lower.contains("how are you") || lower.contains("how do you do") || lower.contains("what's up")) {
            return "I'm here to support you! 😊 More importantly, how are YOU feeling today?"
        }
        
        // --- Name / Who are you? ---
        if (lower.contains("who are you") || lower.contains("what is your name") || lower.contains("tell me about yourself")) {
            return "My name is Thuna. It means 'support' in Tamil. I'm your offline health companion — here to remind you about medicines, share health tips, and offer a kind word whenever you need it."
        }
        
        // --- Symptoms (Headache, Fever, Cold, etc.) ---
        if (lower.contains("headache") || lower.contains("head pain") || lower.contains("migraine")) {
            return "I'm sorry to hear you have a headache. 😔 Please rest in a quiet, dark room and stay hydrated. If the pain is severe or lasts more than 24 hours, please consult a doctor. 🩺"
        }
        
        if (lower.contains("fever") || lower.contains("temperature") || lower.contains("hot")) {
            return "A fever means your body is fighting something. 🌡️ Rest well, drink plenty of fluids, and monitor your temperature. If it goes above 103°F (39.4°C) or lasts more than 3 days, please see a doctor."
        }
        
        if (lower.contains("cold") || lower.contains("cough") || lower.contains("sneeze")) {
            return "Colds are very common. 🤧 Rest, drink warm water or soup, and try to stay warm. If you have trouble breathing or the cough doesn't improve in 7-10 days, please visit a doctor."
        }
        
        if (lower.contains("stomach") || lower.contains("belly") || lower.contains("nausea") || lower.contains("vomit")) {
            return "Stomach issues can be very uncomfortable. 😣 Eat light food like rice porridge (kanji) or toast. Stay hydrated with ORS or coconut water. If you have severe pain, fever, or blood in your vomit, seek medical help immediately."
        }
        
        if (lower.contains("blood pressure") || lower.contains("bp") || lower.contains("hypertension")) {
            return "Blood pressure is important to track. 💓 If you have high BP, avoid salty food, take your medicines on time, and measure your BP regularly. If you feel dizzy or have chest pain, call 108 or visit a doctor urgently."
        }
        
        if (lower.contains("diabetes") || lower.contains("sugar") || lower.contains("blood sugar")) {
            return "Managing diabetes is a daily effort. 🩸 Avoid sweets, eat small meals throughout the day, and take your medicines on time. If your sugar feels too high or too low, check it and consult your doctor."
        }
        
        if (lower.contains("sleep") || lower.contains("insomnia") || lower.contains("can't sleep")) {
            return "Sleep is so important. 😴 Try sleeping at the same time daily, avoid screens before bedtime, drink warm milk, and try deep breathing. If you can't sleep for more than a week, talk to a doctor."
        }
        
        if (lower.contains("stress") || lower.contains("anxiety") || lower.contains("worried") || lower.contains("depression")) {
            return "I'm here for you. 🤗 It's okay to feel stressed or worried. Take deep breaths, talk to someone you trust, and take a walk in fresh air. If you feel very low for many days, please talk to a counselor or doctor."
        }
        
        // --- Health Tips ---
        if (lower.contains("tip") || lower.contains("suggestion") || lower.contains("advice") || lower.contains("suggest")) {
            val tips = listOf(
                "💧 Drink 6-8 glasses of water every day.",
                "🚶 Take a 15-minute walk after meals.",
                "🛌 Sleep for 7-8 hours every night.",
                "🍎 Eat fruits and vegetables daily.",
                "💊 Never skip your medicines.",
                "🧘 Practice deep breathing to reduce stress.",
                "☀️ Get 15 minutes of morning sunlight.",
                "🦷 Brush your teeth twice a day.",
                "🧠 Keep your mind active — read or talk to family.",
                "❤️ Check your BP and sugar regularly."
            )
            return "💡 Health Tip: ${tips.random()}"
        }
        
        // --- Medicines ---
        if (lower.contains("medicine") || lower.contains("pill") || lower.contains("medication") || lower.contains("tablet")) {
            return "Medicines are very important. 💊 Please take them exactly as your doctor prescribed. If you have questions or side effects, always ask a doctor or pharmacist."
        }
        
        // --- Doctor / Hospital ---
        if (lower.contains("doctor") || lower.contains("hospital") || lower.contains("clinic")) {
            return "If you need to see a doctor, don't delay. 🏥 It's always better to be safe. If you have a serious emergency, call 108 (ambulance) immediately."
        }
        
        // --- Age / Personal ---
        if (lower.contains("old") || lower.contains("age")) {
            return "Age is just a number, but health matters at every stage. 😊 I'm here to help you stay well, no matter your age."
        }
        
        // --- Thank you / Thanks ---
        if (lower.contains("thank") || lower.contains("thanks") || lower.contains("thanks a lot")) {
            return "You're most welcome! 🤗 I'm always here for you. Take care of yourself."
        }
        
        // --- Goodbye / Bye ---
        if (lower.contains("bye") || lower.contains("goodbye") || lower.contains("see you") || lower.contains("exit") || lower.contains("quit")) {
            return "Bye-bye! 🥰 Stay healthy and happy. Come back anytime you need me."
        }
        
        // --- Default / Catch-all ---
        return "I'm Thuna, your health companion. You can ask me for health tips 💡, medication reminders 💊, or tell me how you're feeling. Remember: I'm not a doctor — always consult a professional for serious concerns. How can I help you today?"
    }

    // ==================== RULE ENGINE (FOR EXACT COMMANDS) ====================

    private fun handleRuleBased(input: String): IntentResult? {
        val lowercase = input.lowercase().trim()
        
        // Medication Reminder
        if (lowercase.contains("remind") && (lowercase.contains("medicine") || lowercase.contains("pill") || lowercase.contains("medication"))) {
            val medicine = extractMedicineName(input) ?: "your medicine"
            val time = extractTime(input) ?: "morning"
            return IntentResult.MedicationReminder(medicine, time)
        }
        
        // Set Reminder / Alarm
        if (lowercase.contains("set reminder") || lowercase.contains("set alarm") || 
            lowercase.contains("remind me") && !lowercase.contains("medicine")) {
            val time = extractTime(input) ?: "30 minutes"
            return IntentResult.Alarm(time, input)
        }
        
        // Health Tip (explicit request)
        if (lowercase.contains("health tip") || lowercase.contains("give me a tip") || 
            lowercase.contains("suggest something") || lowercase.contains("tell me a tip")) {
            val tips = listOf(
                "💧 Drink 6-8 glasses of water every day.",
                "🚶 Take a 15-minute walk after meals.",
                "🛌 Sleep for 7-8 hours every night.",
                "🍎 Eat fruits and vegetables daily.",
                "💊 Never skip your medicines.",
                "🧘 Practice deep breathing to reduce stress.",
                "☀️ Get 15 minutes of morning sunlight.",
                "🦷 Brush your teeth twice a day.",
                "🧠 Keep your mind active — read or talk to family.",
                "❤️ Check your BP and sugar regularly."
            )
            return IntentResult.HealthTip(tips.random())
        }
        
        return null
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun extractMedicineName(text: String): String? {
        val patterns = listOf(
            Regex("remind me to take (.*?)(?: at| in|$)"),
            Regex("medicine (.*?)(?: at| in|$)"),
            Regex("remind me about (.*?)(?: at| in|$)"),
            Regex("take (.*?)(?: at| in|$)")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val result = match.groupValues[1].trim()
                if (result.isNotEmpty() && !result.matches(Regex("\\d{1,2}.*"))) {
                    return result
                }
            }
        }
        return null
    }

    private fun extractTime(text: String): String? {
        val timePatterns = listOf(
            Regex("(\\d{1,2}:\\d{2})"),
            Regex("(\\d{1,2}) (?:am|pm|AM|PM)"),
            Regex("(morning|afternoon|evening|night|noon|midnight)"),
            Regex("at (morning|afternoon|evening|night|noon|midnight)"),
            Regex("in the (morning|afternoon|evening|night)"),
            Regex("(\\d{1,2}) o'?clock")
        )
        for (pattern in timePatterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }
}
