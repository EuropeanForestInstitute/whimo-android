# 🌍 WHIMO App

Mobile and web application designed to help supply chain actors collect and transfer geolocation data of commodity production areas to support compliance with the **EU Regulation on Deforestation-Free Products (EUDR)**.

---

## 🧩 Overview

The **EUDR**, effective from the end of 2025, requires companies importing commodities (e.g., coffee, cocoa, etc.) into the EU to prove that their products are:
- **Deforestation-free** — produced on land not deforested after 2020
- **Legally sourced** — compliant with local production laws

Many supply chain actors lack reliable **geolocation data** to meet these due diligence requirements.  
**WHIMO App** addresses this gap by providing a simple, secure, and interoperable solution for collecting, storing, and sharing geolocation data across conventional supply chains.

---

## 🎯 Purpose

WHIMO enables:
- **Data collection** — farmers, cooperatives, and traders can easily record land coordinates
- **Data transfer** — geolocation and transaction data are securely shared across the supply chain
- **EUDR readiness** — helps all actors comply with EU due diligence requirements without adding complexity

The solution focuses on **accessibility, privacy, and interoperability**, allowing smaller supply chain actors to participate in traceability systems.

---

## 🚀 Key Features

- Secure user authentication and role-based access
- Geolocation data capture (GPS-based)
- Recording of supply chain transactions
- Encrypted data transfer with user consent
- Offline functionality and local caching
- Interoperability with existing traceability systems
- Basic communication module for connected actors

---

## 🧱 Architecture

The project follows a layered **MVVM + Interactor** architecture with clear separation of concerns:

| Layer | Description |
|-------|--------------|
| **View** | Jetpack Compose screens (functions with suffix `Screen`) responsible for UI rendering and user interaction. |
| **Contract** | Defines screen bindings and communication contracts:<br>• `data class Binding` – UI state container<br>• `sealed class Event` – user events<br>• `sealed class Effect` – one-time side effects |
| **ViewModel** | Handles state management, event processing, and triggers domain logic via Interactors. |
| **Interactor** | Contains business logic and coordinates data operations from repositories. |
| **Repository** | Responsible for data access (local via Room and remote via Retrofit). |

This structure improves testability, readability, and maintainability.

---

## 🛠 Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose  
- **Architecture:** MVVM + Interactor + Repository  
- **Networking:** Retrofit  
- **Database:** Room  
- **Dependency Injection:** Koin  
- **Cloud Services:** Firebase (Analytics, Crashlytics)

---

## 🚀 Getting Started

### Requirements
- Android Studio **Ladybug** (or newer)  
- JDK **17** (ensure correct version in *Project Structure*)  
- Minimum SDK: `26`

---

### 1️⃣ Clone the project
```bash
git clone https://github.com/<your-username>/<your-repo>.git
```

### 2️⃣ Prepare `config` folder

In the project root, create a folder named **`config/`** and add the following files:

#### 🗝 `signing.properties`
```properties
release_storeFile=path/to/your-release.jks
release_storePassword=your_password
release_keyAlias=your_alias
release_keyPassword=your_password

debug_storeFile=path/to/your-debug.jks
debug_storePassword=your_password
debug_keyAlias=your_alias
debug_keyPassword=your_password
```

#### 🔐 `secrets.properties`

Copy [`config/secrets.properties.example`](config/secrets.properties.example) to
`config/secrets.properties` and fill in both groups:

```properties
DEBUG_MAPS_API_KEY=your_dev_Google_Maps_API_key
DEBUG_GOOGLE_AUTH_CLIENT_ID=your_dev_Web_application_OAuth_client_ID

RELEASE_MAPS_API_KEY=your_prod_Google_Maps_API_key
RELEASE_GOOGLE_AUTH_CLIENT_ID=your_prod_Web_application_OAuth_client_ID
```

#### 🔐 `build.properties`
```properties
release_base_url="https://api_url"
release_test_base_url="https://api_url/test/"
release_default_country_code="CM"
release_default_location_latitude=4.106729
release_default_location_longitude=9.3972812
release_terms_of_use="https://your_site/terms"
release_feedback_email="support@example.com"
release_captcha_url="https://your_site/captcha"

debug_base_url="https://api_url"
debug_test_base_url="https://api_url/test/"
debug_default_country_code="CM"
debug_default_location_latitude=4.106729
debug_default_location_longitude=9.3972812
debug_terms_of_use="https://your_site/terms"
debug_feedback_email="support@example.com"
debug_captcha_url="https://your_site/captcha"
```

> ⚠️ These files are required for the project to build successfully.  
> They are already excluded in `.gitignore` — do **not** commit them.

---

### 3️⃣ Configure dev and prod services

| Build type | Application ID | Launcher name | Version |
|---|---|---|---|
| `debug` | `com.whimo.dev` | WHIMO DEV (orange icon with DEV marker) | `<version>-dev` |
| `release` | `com.whimo` | WHIMO | `<version>` |

Both apps can be installed together. `debug` uses the `debug_*` / `DEBUG_*`
settings in the files above; `release` uses `release_*` / `RELEASE_*`.

1. **Signing certificates:** run `./gradlew :app:signingReport` to get SHA-1 and
   SHA-256 for the keys configured in `config/signing.properties`. For builds
   delivered through Google Play, use the **App signing key certificate**
   fingerprints from Play Console → App integrity. Include each signing key used
   by developers or CI.
2. **Firebase:** register `com.whimo.dev` and verify the existing `com.whimo` app,
   adding their corresponding SHA-1 and SHA-256 fingerprints. Download each app's
   configuration to `app/src/debug/google-services.json` and
   `app/src/release/google-services.json`, respectively. Move any existing production
   `app/google-services.json` to the release path. Keep these files out of Git and
   supply them in CI too. Both registrations can use the existing Firebase project.
3. **Google sign-in:** verify an Android OAuth client for each package + signing
   certificate SHA-1 pair in the same Google Cloud project as its Web OAuth client.
   Set `DEBUG_GOOGLE_AUTH_CLIENT_ID` and `RELEASE_GOOGLE_AUTH_CLIENT_ID` to the
   corresponding **Web application** client IDs accepted by each backend. Both
   builds may share a Web client ID if both backends accept it.
4. **Maps:** enable Maps SDK for Android and billing in the project that owns each
   Maps key. Restrict each key to Maps SDK for Android and the matching Android
   package + signing certificate SHA-1 pairs. Put the keys in `DEBUG_MAPS_API_KEY`
   and `RELEASE_MAPS_API_KEY` in `config/secrets.properties`.

If using a separate development Firebase project, also configure its OAuth clients,
Remote Config parameters, and backend FCM credentials.

### 4️⃣ Open and run

1. Open the project in **Android Studio** and let Gradle sync.
2. Select `debug` or `release` in **Build Variants**, then build and run on an emulator
   or physical device. To build both APKs from the project root:

   ```bash
   ./gradlew :app:assembleDebug :app:assembleRelease
   ```

3. Verify the expected backend, Google sign-in, Maps, push notifications, and Remote
   Config in each app. Test production through a Play testing track to cover Play
   App Signing.

---

## 🧪 Development Notes

- Clean and modular architecture with Compose UI separation  
- Offline caching handled via Room  
- Network sync via Retrofit  
- Dependency injection using Koin

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).
