# QuickChat

Message any WhatsApp number directly, without saving it to your contacts.

[![CI](https://github.com/salehbagomri/quickchat/actions/workflows/ci.yml/badge.svg)](https://github.com/salehbagomri/quickchat/actions/workflows/ci.yml)
![Version](https://img.shields.io/badge/version-2.1.0-blue)
![Platform](https://img.shields.io/badge/platform-Android-lightgrey)
![License](https://img.shields.io/badge/license-Proprietary-red)

## Overview

QuickChat is a lightweight, privacy-first utility for starting a WhatsApp
conversation with any phone number without first adding it to your address book.
It is designed for business owners, delivery and sales teams, and anyone who
contacts new numbers throughout the day.

The application runs entirely on the device. It has no backend, performs no
tracking, and contains no advertising or third-party analytics. All data —
history, templates, and favorites — is stored locally and never leaves the
device.

## Features

- Direct messaging to any number through the official WhatsApp or WhatsApp Business app.
- Country code selection covering 195+ countries, with the default derived from the device locale.
- Message templates with categories, including a built-in set and full user management.
- Recent history with search and day grouping.
- Favorites for one-tap access to frequent numbers.
- Home-screen widget and launcher shortcuts for quick access.
- Clipboard detection and shared-text handling to auto-fill numbers.
- wa.me link generation, sharing, and QR code generation.
- Clickable deep links and Android App Links.
- Guided broadcast for sending the same message to multiple recipients manually.
- Light, dark, and system themes.
- Full Arabic and English interfaces with RTL support, plus Spanish, Hindi, Portuguese, Indonesian, Urdu, and Turkish.

## Technology

- **Framework:** Flutter (Dart)
- **State management:** flutter_bloc, equatable
- **Local storage:** Hive, shared_preferences
- **Localization:** flutter_localizations, intl
- **Key packages:** url_launcher, country_code_picker, qr_flutter, share_plus, share_handler, app_links, home_widget, quick_actions, flutter_native_splash

## Architecture

The project follows a clean, layered architecture:

```
lib/
├── core/        Theme, routing, constants, shared widgets, utilities
├── data/        Models, services, and local storage
├── features/    Feature modules (home, history, templates, favorites,
│                settings, broadcast, onboarding, privacy)
└── l10n/        Localization resources
```

State is managed with the BLoC (Cubit) pattern. Each feature owns its cubit,
state, screen, and widgets. Services are singletons and provide the single
source of truth for storage and WhatsApp integration.

## Getting Started

### Prerequisites

- Flutter SDK 3.9.2 or later
- Android Studio or Visual Studio Code with the Flutter plugin

### Setup

```bash
git clone https://github.com/salehbagomri/quickchat.git
cd quickchat
flutter pub get
flutter gen-l10n
dart run build_runner build --delete-conflicting-outputs
flutter run
```

## Building

```bash
# Android App Bundle (for Google Play)
flutter build appbundle --release

# Android APK
flutter build apk --release
```

Output:

- App Bundle: `build/app/outputs/bundle/release/app-release.aab`
- APK: `build/app/outputs/flutter-apk/app-release.apk`

## Testing

```bash
flutter analyze
flutter test
```

Continuous integration runs analysis and tests on every push and pull request
to `main`.

## Project Information

| Property      | Value                       |
| ------------- | --------------------------- |
| Package ID    | `com.bagomri.quickchat`     |
| Version       | 2.1.0 (build 5)             |
| Minimum SDK   | 21 (Android 5.0)            |
| Target SDK    | 35 (Android 15)             |
| Permissions   | `INTERNET`                  |

## Privacy

QuickChat collects no personal data. There is no backend, no tracking, and no
advertising. History, templates, and favorites are stored locally on the device
and can be cleared at any time from within the application. The only permission
required is `INTERNET`, which is used solely to open WhatsApp links.

## License

Copyright (c) 2026 Saleh Bagomri. All rights reserved.

This repository is publicly visible for reference and transparency. It is not
open-source software, and no rights are granted to use, copy, modify, or
redistribute it without prior written permission. See [LICENSE](LICENSE) for
details.

## Contact

- **Developer:** Saleh Bagomri
- **Website:** [www.bagomri.com](https://www.bagomri.com)
- **Email:** s.bagomri@gmail.com
- **Google Play:** [QuickChat](https://play.google.com/store/apps/details?id=com.bagomri.quickchat)
