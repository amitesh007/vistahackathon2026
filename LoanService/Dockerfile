# Multi-stage build for LoanService
#
# Stage 1 (builder): compile + package the JAR with Gradle
# Stage 2 (runtime): copy only the JAR into a slim JRE image
#
# The CI workflow (docker-build-deploy-codespaces.yml) relies on this file.
# You can also build locally:
#   docker build -t loan-service .

# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper and dependency manifests first (layer-cache friendly)
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

# Download dependencies (cached when build.gradle hasn't changed)
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true

# Copy the full source and build the fat JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/LoanService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
