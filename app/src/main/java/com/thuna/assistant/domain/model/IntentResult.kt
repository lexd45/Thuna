package com.thuna.assistant.domain.model

sealed class IntentResult {
    data class Alarm(val time: String, val message: String) : IntentResult()
    data class HealthTip(val tip: String) : IntentResult()
    data class MedicationReminder(val medicine: String, val time: String) : IntentResult()
    data class GeneralResponse(val response: String) : IntentResult()
    object Unknown : IntentResult()
}
