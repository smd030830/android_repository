# DogLog Spring Boot Backend

This backend replaces the JSP/Tomcat server while keeping the Android app's existing request paths:

- `GET /ServerProject/GetDogList.jsp`
- `POST /ServerProject/UserLogin.jsp`
- `POST /ServerProject/UserRegister.jsp`
- `GET /health`

The backend uses an embedded, file-based H2 database. No MySQL or Docker setup is required.
Data is stored in `backend/data/` and remains available after restarting the server.

Recommended run configuration:

- `DogLog Backend bootRun`

Avoid using the temporary gutter action named like `:backend:com.example...main()`. This server is a long-running process, so if Android Studio cancels that temporary task it may show `Build cancelled`.

Or use the Gradle task from Android Studio:

- `backend > Tasks > application > bootRun`

The equivalent PowerShell command is:

```powershell
$env:JAVA_HOME = "C:\Users\smd08\.jdks\temurin-21.0.10"
.\gradlew.bat :backend:bootRun
```

Verify the server at `http://localhost:8080/health`. The Android emulator uses
`http://10.0.2.2:8080/ServerProject/...` to reach the same server on the host PC.
