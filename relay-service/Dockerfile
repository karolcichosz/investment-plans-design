# Multi-stage build for Relay Service
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the JAR
COPY src src
RUN mvn clean package -DskipTests

# ────────────────────────────────────────────────
# Runtime stage - Alpine + glibc compatibility for snappy-java
# ────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Install gcompat → provides ld-linux-x86-64.so.2 compatibility layer
# needed by the native libsnappyjava.so (glibc-linked)
RUN apk add --no-cache gcompat

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/relay-service-1.0.0.jar app.jar

# Health check (using wget --spider style)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Expose the port your app listens on
EXPOSE 8081

# Run with docker profile (as you already had)
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]