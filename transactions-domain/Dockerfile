# Multi-stage build for Transactions Domain Service
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (good for layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR
COPY src src
RUN mvn clean package -DskipTests -B

# ────────────────────────────────────────────────
# Runtime stage - Alpine + glibc compatibility for snappy-java
# ────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Install gcompat → provides ld-linux-x86-64.so.2
# This fixes the UnsatisfiedLinkError when snappy-java tries to load its native .so
RUN apk add --no-cache gcompat

WORKDIR /app

# Copy the built JAR from the builder stage
# Adjust filename if your final jar name is different
COPY --from=builder /app/target/transactions-0.0.1-SNAPSHOT.jar app.jar

# Health check using actuator (standard Spring Boot location)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Expose the port your app listens on
EXPOSE 8083

# Run with docker profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]