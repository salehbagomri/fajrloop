# FajrLoop

Sleep responsibly, wake up together. An Android application for mutual responsibility to wake up for Fajr prayer in a closed circular chain.

[![Android CI](https://github.com/salehbagomri/fajrloop/actions/workflows/android-ci.yml/badge.svg)](https://github.com/salehbagomri/fajrloop/actions/workflows/android-ci.yml)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Platform](https://img.shields.io/badge/platform-Android-lightgrey)
![License](https://img.shields.io/badge/license-Proprietary-red)

## Overview

FajrLoop is an innovative, native Android application designed to help Muslims wake up for Fajr prayer through peer-to-peer accountability. Users join a closed circular loop (Halqa) where each member is responsible for waking up the next member in the chain. 

When your alarm rings, you must solve a wake-up challenge (Math, Typing, or Shaking) to silence the alarm. Once verified awake, you are tasked with waking up the next brother in your loop. If a member does not wake up or confirm, the chain triggers backup phone calls and notifications, ensuring no one misses the prayer.

The application works both online (real-time Firebase synchronization) and offline (independent prayer calculation and fallback offline TOTP rescue codes).

## Features

- **Circular Accountability Chain:** Closed-loop peer-to-peer wake-up system where each member is the wake-up manager for the next.
- **Multiple Wake-up Challenges:** Includes Math Equation Solver, Shake-to-Wake (using accelerometer), and Text Typing challenges with configurable difficulty levels (Easy, Medium, Hard).
- **Secure Ringing System:** Custom lock-screen kiosk mode with `USE_FULL_SCREEN_INTENT`, home/recents button interception, and smart call watchdog monitoring to prevent users from escaping or bypassing the challenge.
- **Offline-First Resilience:** Integrated `Adhan` library for precise offline prayer calculations based on coordinates/city, and offline-generated TOTP rescue codes (retrieved from your partner) for unlocking alarms when internet connection is lost.
- **Premium Dark Aesthetics:** Sleek, glassmorphic design language featuring an `AnimatedGradientView` (rotating cosmic aura), custom `GlassCardView` layouts, and golden-accented typography.
- **Real-Time Loop Sync:** Real-time chat, awake status, and daily statistics synchronized instantly using Firebase Realtime Database.
- **Strict Permission Gatekeeper:** Guided onboarding flow that validates and mandates all critical Android permissions (Exact Alarms, Notifications, Battery Optimization bypass, and Overlays) on startup to ensure alarm reliability.

## Technology Stack

- **Language:** Native Kotlin
- **Architecture:** MVVM (Model-View-ViewModel) with ViewBinding
- **Local Storage:** SharedPreferences (with startup cache to prevent visual flickers)
- **Database & Auth:** Firebase Realtime Database, Firebase Authentication, and Google Credential Manager (One Tap Sign-In)
- **Prayer Computations:** Adhan Java library
- **Background Tasks:** Android WorkManager and AlarmManager
- **Push Services:** Firebase Cloud Messaging (FCM)

## Architecture

The project follows clean architecture principles separated by concern:

```
app/src/main/java/com/bagomri/fajrloop/
├── alarm/       Alarm scheduling, receiver watchdogs, and sound services
├── auth/        Authentication flow and One-Tap Google credentials helpers
├── data/        Local/remote data repositories, database models, and sync managers
├── ui/          Activities,adapters, viewmodels, and custom UI components
└── utils/       Location, calculation, and helper classes
```

## Getting Started

### Prerequisites

- JDK 17 or later
- Android SDK (API level 21 or later)
- Android Studio (Koala or newer recommended)
- Firebase project credentials (`google-services.json` placed in the `app/` directory)

### Setup

```bash
# Clone the repository
git clone https://github.com/salehbagomri/fajrloop.git
cd fajrloop

# Build and install debug build directly on your connected device
.\gradlew installDebug
```

## Building

```bash
# Generate debug APK
.\gradlew assembleDebug

# Generate release bundle (AAB)
.\gradlew bundleRelease
```

Output:
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release Bundle: `app/build/outputs/bundle/release/app-release.aab`

## Project Information

| Property      | Value                        |
| ------------- | ---------------------------- |
| Package ID    | `com.bagomri.fajrloop`       |
| Min SDK       | 21 (Android 5.0)             |
| Target SDK    | 34 (Android 14)              |
| Core Services | Firebase Database, FCM, Auth |

## Privacy

FajrLoop handles only the necessary information required to operate the loop (display name, profile photo, and halqa association details). Prayer calculations are performed locally on the device using offline GPS coordinates. The application does not share data with third-party advertisement networks or include tracking trackers.

## License

Copyright (c) 2026 Saleh Bagomri. All rights reserved.

This repository is publicly visible for reference and transparency. It is not open-source software, and no rights are granted to use, copy, modify, or redistribute it without prior written permission. See [LICENSE](LICENSE) for details.

## Contact

- **Developer:** Saleh Bagomri
- **Website:** [www.bagomri.com](https://www.bagomri.com)
- **Email:** s.bagomri@gmail.com
