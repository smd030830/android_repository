# DogLog Spring Boot Backend

This backend replaces the JSP/Tomcat server while keeping the Android app's existing request paths:

- `GET /ServerProject/GetDogList.jsp`
- `POST /ServerProject/UserLogin.jsp`
- `POST /ServerProject/UserRegister.jsp`

Run MySQL first, then start the backend from Android Studio.

Recommended run configuration:

- `DogLog Backend bootRun`

Avoid using the temporary gutter action named like `:backend:com.example...main()`. This server is a long-running process, so if Android Studio cancels that temporary task it may show `Build cancelled`.

Or use the Gradle task from Android Studio:

- `backend > Tasks > application > bootRun`

If your Gradle wrapper is restored later, the equivalent command is:

```powershell
docker start my-mysql
.\gradlew.bat :backend:bootRun
```

The app can keep using `http://10.0.2.2:8080/ServerProject/...` in the Android emulator.
