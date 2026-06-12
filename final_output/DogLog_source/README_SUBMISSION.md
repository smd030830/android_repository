# DogLog 제출용 소스

이 폴더는 기말 프로젝트 제출용으로 build 산출물을 제외하고 구성한 소스입니다.

## 실행 순서
1. MySQL에서 doglog DB를 사용할 수 있도록 실행합니다. application.properties의 기본 계정은 doglog / qwer1234 입니다.
2. Android Studio에서 Gradle JDK를 21로 설정합니다.
3. Run Configuration의 Backend Server 또는 Gradle :backend:bootRun으로 서버를 실행합니다.
4. /health가 ok를 반환하면 app 구성을 에뮬레이터에서 실행합니다.

## JSP 코드 위치
기존 JSP 제출 요구를 맞추기 위해 apache-tomcat-9.0.117/webapps/ServerProject에 있던 JSP 원본을 jsp/ServerProject 폴더에도 포함했습니다.
현재 앱 실행은 Spring Boot 백엔드가 같은 /ServerProject/*.jsp 경로를 처리하는 방식입니다.
