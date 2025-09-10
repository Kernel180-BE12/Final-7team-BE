# 빌드 단계 (Gradle + JDK 21)
FROM gradle:8.14-jdk21 AS builder

WORKDIR /app

# Gradle 캐시 최적화 (dependencies 먼저 받아서 빌드 속도 향상)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# 런타임 단계 (경량 JDK 21)
FROM openjdk:21-jdk-slim
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 포트 오픈
EXPOSE 8080

# Spring Boot 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
