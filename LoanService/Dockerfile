# Single-stage: uses the pre-built bootJar from local Gradle build
# Run `gradlew bootJar` before building this image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/LoanService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
