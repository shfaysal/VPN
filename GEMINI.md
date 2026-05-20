# Project: VPN Android App (com.example.vpn)

## Purpose
Build an Android VPN client using `VpnService` with a modern Compose UI.
Gemini should act like a senior Android engineer + QA partner:
- implement features safely and incrementally
- prefer minimal, reviewable diffs
- always include build/run/test steps for changes
- diagnose crashes using Logcat and stack traces

---

## Tech Stack
- Language: Kotlin
- UI: Jetpack Compose (Material 3)
- Build: Gradle Kotlin DSL + Version Catalogs (`libs.versions.toml`)
- Min SDK: 24
- Target/Compile SDK: 36
- Architecture: (choose one) MVVM + StateFlow / MVI / simple ViewModel-first

---

## Current Status
### Implemented
- Compose UI scaffold + dashboard screen ("Aura VPN" branding)
- VPN permission request flow in `MainActivity`
- `MyVpnService` basic skeleton with:
    - Foreground service notification
    - TUN interface establishment
    - AdGuard DNS configuration (94.140.14.14, 94.140.15.15)
    - Dummy subnet routing (192.168.99.0/24) for safe testing

### Not Implemented Yet
- Real tunneling/protocol (e.g., WireGuard/OpenVPN)
- Background packet processing loop (read/write to TUN interface)
- Config import (.conf / QR / share intent)
- Kill switch / Always-on VPN support
- Split tunneling
- Robust error handling + reconnect logic

---

## Key Modules & Files
- `app/src/main/java/com/example/vpn/`
  - `MainActivity.kt` (permission + navigation entry)
  - `vpn/` (VpnService + tunnel/session code goes here)
  - `ui/` (Compose screens/components)
  - `data/` (configs, repositories, persistence)
- `app/src/main/AndroidManifest.xml`
  - MUST declare `VpnService` and required permissions
- `gradle/libs.versions.toml` for dependencies

---

## Conventions & Engineering Rules
### Code Style
- Compose-only UI (no XML)
- Use `StateFlow`/`MutableStateFlow` for UI state
- Avoid over-engineering: prefer clear, small classes
- Use sealed types for connection state:
  - `Disconnected`, `Connecting`, `Connected`, `Error(...)`

### Logging
- Use consistent tags:
  - `VPN`, `TUNNEL`, `PERMISSION`, `UI`
- Log key lifecycle events:
  - permission granted/denied
  - service start/stop
  - connect/disconnect
  - errors with stacktrace

### Security & Privacy
- Never log secrets:
  - private keys, tokens, full config contents
- Redact IPs/user identifiers in logs if possible
- Keep debug-only helpers behind `BuildConfig.DEBUG`

### Networking/VPN Notes
- `VpnService.Builder` must be configured carefully:
  - addresses, routes, DNS, MTU
- Always handle:
  - `onRevoke()`
  - service restart scenarios
  - foreground service requirements (if applicable)
- Add clear user-visible error messages when connect fails

---

## Build & Run Commands
- Build debug:
  ```bash
  ./gradlew assembleDebug

