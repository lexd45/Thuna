# 🧠 Thuna – Offline Voice-First AI Health Companion

> **Thuna** (meaning "support" in Tamil) is an offline, voice-first AI health companion for India's 173M+ elderly citizens – especially those in rural areas with poor internet and low digital literacy.

[![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![TensorFlow Lite](https://img.shields.io/badge/TensorFlow%20Lite-FF6F00?style=flat&logo=tensorflow&logoColor=white)](https://www.tensorflow.org/lite)
[![Hackathon](https://img.shields.io/badge/iQOO%20Hackathon%202026-Chennai-blue)](https://iqoo.reskilll.com/)

## 🎯 The Problem

- **173M+** elderly Indians – **70%** live in rural areas
- **32%** lack reliable internet connectivity
- **85%** cannot read or type on smartphones
- **31%** travel >30km for basic healthcare

## 💡 The Solution

Thuna is a **100% offline, voice-first** AI companion that:

- 🎤 **Voice-First** – Speaks Tamil, Hindi, and English. No typing required.
- 📡 **100% Offline** – Works without internet. Perfect for rural India.
- 🔒 **Privacy-First** – All AI runs on-device. Health data NEVER leaves the phone.
- 🧠 **On-Device AI** – Gemma 2B LLM runs via TFLite + XNNPACK on the Snapdragon NPU.

> ⚠️ **Thuna is NOT a medical diagnosis app.** It is a supportive companion that reminds, guides, and encourages users to consult real healthcare professionals for serious concerns.

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| **UI** | Jetpack Compose + Kotlin |
| **LLM** | Gemma 2B (Google) – INT4 quantized |
| **Inference** | TensorFlow Lite + XNNPACK delegate |
| **Speech-to-Text** | Vosk (offline) |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Hilt |

## 📱 Features

- ✅ Voice input with offline STT
- ✅ Text input fallback
- ✅ Medication reminders
- ✅ Health tips & wellness advice
- ✅ Symptom guidance (headache, fever, cold, etc.)
- ✅ 100% offline – works in Airplane Mode
- ✅ Hybrid intelligence: Rule Engine (0-50ms) + LLM

## 🏗️ Architecture

User Speaks/Types → Whisper/Vosk (STT) → Rule Engine (Fast Path) → Gemma 2B (LLM) → TTS Response

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest)
- Android SDK (API 26+)
- Git

### Clone & Build

```bash
git clone https://github.com/lexd45/Thuna.git
cd Thuna
```

### Model Setup
1. Download the Gemma 2B model from Kaggle / Hugging Face.
2. Place it at `app/src/main/res/raw/gemma_2b_it_gpu_int4.task`.
3. Build and run the app in Android Studio.

## 📊 Hackathon Fit

| Criteria | How Thuna Delivers |
|----------|-------------------|
| **Phone-First** | 100% mobile-native, uses mic & speaker |
| **AI-Native** | Gemma 2B on-device via TFLite + XNNPACK |
| **Offline-First** | Zero cloud dependency |
| **Real-World Impact** | Solves India's elderly healthcare access crisis |
| **Privacy** | No data ever leaves the device |

## 👤 Team

**Team return 0** – One builder, one mission, one working prototype.

## 📄 License

This project is built for the **iQOO Hackathon 2026 Chennai Battle**. All rights reserved.

Built with ❤️ for India's elderly.
