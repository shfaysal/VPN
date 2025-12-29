# Project Context: VPN Android App

## Overview
This is an Android application project named **VPN** (`com.example.vpn`).
Currently, it is a **fresh project scaffold** generated with Android Studio, configured for **Modern Android Development**.

## Technology Stack
- **Language:** Kotlin (`2.0.21`)
- **UI Framework:** Jetpack Compose (Material 3 Design)
- **Build System:** Gradle with Kotlin DSL (`.kts`) & Version Catalogs (`libs.versions.toml`)
- **Minimum SDK:** 24 (Android 7.0 Nougat)
- **Target/Compile SDK:** 36 (Android 15)

## Key Directories & Files
- **`app/src/main/java/com/example/vpn/`**: Contains the source code.
  - `MainActivity.kt`: The entry point activity. Currently displays a basic "Hello Android" screen.
  - `ui/theme/`: Compose theming definitions (Color, Type, Theme).
- **`app/src/main/AndroidManifest.xml`**: Application manifest.
  - *Note:* No `VpnService` is currently declared. This needs to be added for actual VPN functionality.
- **`gradle/libs.versions.toml`**: Dependency management and version definitions.
- **`build.gradle.kts`**: Root and Module-level build configurations.

## Building and Running
The project uses the Gradle Wrapper.

- **Build Debug APK:**
  ```bash
  ./gradlew assembleDebug
  ```

- **Run Unit Tests:**
  ```bash
  ./gradlew test
  ```

- **Run Instrumented Tests:**
  ```bash
  ./gradlew connectedAndroidTest
  ```

- **Lint Check:**
  ```bash
  ./gradlew lint
  ```

## Development Status
- **Current State:** Functional UI Prototype with VpnService Integration.
- **Implemented:**
  - Modern Dashboard UI with animated connection toggle.
  - `MyVpnService` implementation for managing the VPN interface.
  - VPN Permission handling in `MainActivity`.
- **Pending Implementation:**
  - Real packet routing logic (WireGuard or similar).
  - Config file parsing (.conf/QR Code).
  - Kill Switch and Split Tunneling features.

## Conventions
- Follow **Material 3** guidelines for UI.
- Use **Jetpack Compose** for all UI development.
- Manage dependencies via **Version Catalogs** (`libs.versions.toml`).
