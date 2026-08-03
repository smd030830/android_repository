# DogLog

DogLog consists of an Android app and a Spring Boot backend. The backend uses a
local H2 file database, so MySQL, Docker, and the bundled legacy Tomcat are not
required.

## Run in Android Studio

1. Set the Gradle JDK to Android Studio JBR 21. This project stores the local
   path in `.gradle/config.properties`.
2. Run the shared `DogLog Backend bootRun` configuration.
3. Confirm `http://localhost:8080/health` returns `ok`.
4. Start the `app` configuration on an Android emulator.

The emulator accesses the host backend through `10.0.2.2:8080`. Database files
are created under `backend/data/` on first startup.

## Firebase realtime chat

The app now uses Firebase Realtime Database for one-to-one chat between adopters
and foster users.

1. Create or open a Firebase project.
2. Add an Android app with package name `com.example.dogapp`.
3. Download `google-services.json` and place it in `app/google-services.json`.
4. Enable Realtime Database in the Firebase console.
5. For a class demo only, allow authenticated project testers to read and write
   `doglog_chats`. Tighten these rules before sharing the app publicly.

Without `app/google-services.json`, the Android app still builds, but the chat
screen shows a setup message instead of connecting.

## Command line build

```powershell
.\gradlew.bat :app:assembleDebug :backend:test
```

## Install on emulator

Start an Android Studio emulator first, then run:

```powershell
.\deploy_doglog_debug.ps1
```

The installable debug APK is created at
`app/build/outputs/apk/debug/app-debug.apk`.

If the emulator stays `offline`, make sure Android Studio and command-line
tools both use the SDK in `local.properties`:

```text
sdk.dir=C\:\\Android\\Sdk
```

Using a different SDK path can start a different `adb` server and prevent the
Run button from installing the app.
