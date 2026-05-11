# JobMatch — Kotlin Multiplatform Mobile App

A job-matching mobile application built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, targeting Android (and iOS). Users authenticate via OTP, build a profile with skills and an optional audio/video intro, browse paginated job listings, and apply to jobs — all backed by the [JobMatch Spring Boot API](https://github.com/Abhishekpandey4287/jobmatch_backend).

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Compose Multiplatform (Material 3) |
| Networking | Ktor Client + Kotlin Serialization |
| Auth token storage | DataStore Preferences |
| Navigation | Jetpack Navigation Compose |
| State management | ViewModel + StateFlow |
| DI | Manual object graph (`AppDependencies`) |
| Platform targets | Android · iOS |

---

## Prerequisites

| Tool | Minimum version |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or newer |
| JDK | 17 |
| Xcode (iOS only) | 15+ |
| Kotlin Multiplatform plugin | Installed in Android Studio |
| Android SDK | API 24+ (minSdk) |
| Gradle | 8.x (via wrapper — no install needed) |

---

## Setup & Running

### 1. Clone the repository

```bash
git clone https://github.com/Abhishekpandey4287/jobmatch.git
cd jobmatch
```

### 2. Point the app at your backend

Open `composeApp/src/commonMain/kotlin/com/jobmatch/di/AppDependencies.kt` and update the two `BASE_URL` constants to your machine's local IP address (the same IP your backend runs on):

```kotlin
private const val BASE_URL = "http://YOUR_LOCAL_IP:8080/"
```

> **Finding your IP:** On macOS/Linux run `ifconfig | grep "inet "`. On Windows run `ipconfig`.  
> Use your LAN IP (e.g. `192.168.x.x`), not `localhost` — physical devices can't reach `localhost` on your dev machine.

Also update the same IP in `composeApp/src/androidMain/kotlin/com/jobmatch/data/api/createHttpClient.kt` if the `DefaultRequest` block has a hardcoded URL.

### 3. Make sure the backend is running

Start the Spring Boot server before launching the app. See the [backend README](https://github.com/Abhishekpandey4287/jobmatch_backend) for instructions.

### 4a. Run on Android

Connect a physical device or start an emulator, then either:

- **Android Studio:** Select the `composeApp` run configuration from the toolbar and press ▶
- **Terminal (macOS/Linux):**
  ```bash
  ./gradlew :composeApp:installDebug
  ```
- **Terminal (Windows):**
  ```bat
  .\gradlew.bat :composeApp:installDebug
  ```

> If using an **emulator**, replace `YOUR_LOCAL_IP` with `10.0.2.2` — the special alias that routes to the host machine from the Android emulator.

### 4b. Run on iOS (macOS only)

Open the `iosApp` directory in Xcode:

```bash
open iosApp/iosApp.xcodeproj
```

Select a simulator or connected device and press ▶ in Xcode. Alternatively use the run configuration in Android Studio with the KMP plugin installed.

### 5. Demo login

The backend runs in mock OTP mode by default. Any phone number you enter will receive OTP **`123456`** — no real SMS is sent.

---

## Project Structure

```
jobmatch/
├── composeApp/
│   └── src/
│       ├── commonMain/kotlin/com/jobmatch/
│       │   ├── App.kt                        # Root composable — applies theme + nav host
│       │   ├── data/
│       │   │   ├── api/                      # Ktor HTTP client + JobMatchApiService
│       │   │   ├── local/                    # DataStore (SessionManager, token persistence)
│       │   │   ├── model/                    # Serializable DTOs + toDomain() mappers
│       │   │   └── repository/               # Repository implementations (Impl classes)
│       │   ├── di/
│       │   │   └── AppDependencies.kt        # Manual DI — resettable singleton graph
│       │   ├── domain/
│       │   │   ├── model/                    # Pure domain models (Job, User, Application…)
│       │   │   ├── repository/               # Repository interfaces (contracts)
│       │   │   └── usecase/                  # One use case per business operation
│       │   ├── platform/
│       │   │   └── MediaPicker.kt            # expect declaration for file picking
│       │   └── presentation/
│       │       ├── auth/                     # PhoneScreen, OtpScreen + ViewModels
│       │       ├── applications/             # MyApplicationsScreen + ViewModel
│       │       ├── components/               # Shared composables (JobCard, ErrorView…)
│       │       ├── jobs/                     # JobFeedScreen, JobDetailScreen + ViewModels
│       │       ├── navigation/               # JobMatchNavHost, Routes
│       │       ├── profile/                  # ProfileSetupScreen, ProfileEditScreen + VMs
│       │       └── theme/                    # Colors, typography, JobMatchTheme
│       ├── androidMain/kotlin/com/jobmatch/
│       │   ├── MainActivity.kt
│       │   ├── data/local/                   # Android DataStore initialisation
│       │   └── platform/
│       │       └── MediaPicker.android.kt    # actual — reads bytes via ContentResolver
│       └── iosMain/kotlin/com/jobmatch/
│           ├── MainViewController.kt
│           ├── data/local/                   # iOS DataStore (NSDocumentDirectory)
│           └── platform/
│               └── MediaPicker.ios.kt        # actual — iOS file picker (UIDocumentPicker)
├── iosApp/                                   # Xcode project entry point
├── gradle/                                   # Gradle wrapper
├── build.gradle.kts
└── settings.gradle.kts
```

### Architecture

The app follows a strict **Clean Architecture** layering: `presentation → domain → data`. The domain layer (`model/`, `repository/`, `usecase/`) is pure Kotlin with zero platform or framework imports — it knows nothing about Ktor, Compose, or Android. The data layer implements the domain's repository interfaces using Ktor for networking and DataStore for persistence. Presentation consists of `ViewModel + StateFlow + Composable` triples: the ViewModel holds all business logic, exposes a single immutable `UiState` object, and the composable reacts to it.

Dependency wiring is handled by a hand-rolled `AppDependencies` singleton rather than a DI framework. Critically, the HTTP client and all downstream objects are **resettable** — calling `AppDependencies.reset()` on logout tears down the existing Ktor client (which caches the JWT in memory) and null-clears all references, so the next login constructs a fresh client bound to the new user's session. The `expect/actual` mechanism is used only for the two genuinely platform-specific concerns: DataStore initialisation and the file picker (which needs `ContentResolver` on Android).

---

## Key API Endpoints (consumed)

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/auth/otp/send` | Request OTP |
| `POST` | `/api/v1/auth/otp/verify` | Verify OTP → JWT |
| `GET` | `/api/v1/users/me` | Fetch profile |
| `PUT` | `/api/v1/users/me/profile` | Update name/skills/location |
| `POST` | `/api/v1/users/me/audio` | Upload audio/video intro |
| `DELETE` | `/api/v1/users/me/audio` | Remove intro |
| `GET` | `/api/v1/jobs` | Paginated job list (+ search) |
| `GET` | `/api/v1/jobs/{id}` | Job detail |
| `POST` | `/api/v1/applications/jobs/{id}/apply` | Apply to job |
| `GET` | `/api/v1/applications/my` | My applications |

All authenticated requests attach the JWT automatically via Ktor's `Auth { bearer { } }` plugin.

---

## Related Repository

**Backend:** [Abhishekpandey4287/jobmatch_backend](https://github.com/Abhishekpandey4287/jobmatch_backend) — Spring Boot REST API with PostgreSQL, JWT auth, OTP login, and file upload.
