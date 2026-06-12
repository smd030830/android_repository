# DogLog

DogLog consists of an Android app and a Spring Boot backend. The backend uses a
local H2 file database, so MySQL, Docker, and the bundled legacy Tomcat are not
required.

## Run in Android Studio

1. Set the Gradle JDK to the installed Temurin 21 JDK. This project stores the
   local path in `.gradle/config.properties`.
2. Run the shared `DogLog Backend bootRun` configuration.
3. Confirm `http://localhost:8080/health` returns `ok`.
4. Start the `app` configuration on an Android emulator.

The emulator accesses the host backend through `10.0.2.2:8080`. Database files
are created under `backend/data/` on first startup.

## Command line build

```powershell
$env:JAVA_HOME = "C:\Users\smd08\.jdks\temurin-21.0.10"
.\gradlew.bat :app:assembleDebug :backend:test
```
