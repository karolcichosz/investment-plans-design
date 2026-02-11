# Multi-stage build for Execution Service
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (good for layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the JAR
COPY src src
RUN mvn clean package -DskipTests

# ────────────────────────────────────────────────
# Runtime stage - Alpine + glibc compatibility for snappy-java native lib
# ────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Install gcompat → provides ld-linux-x86-64.so.2 (glibc dynamic linker compatibility)
# This fixes the UnsatisfiedLinkError when snappy-java tries to load its native .so
RUN apk add --no-cache gcompat

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/execution-service-1.0.0.jar app.jar

# Health check – note: changed /health → /actuator/health (more standard for Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Expose the port your app listens on
EXPOSE 8082

# Run with docker profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]