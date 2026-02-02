# ============
# Build stage
# ============
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY . .

# Build Spring Boot JAR
# If using Maven:
RUN ./mvnw -q -DskipTests package

# ============
# Run stage
# ============
# Uses a lightweight JRE-only image, compatible with ARM and x86
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the fat jar from the build stage
COPY --from=builder /app/target/*.jar /app/app.jar

# Create directories for persistent storage
RUN mkdir -p /data/photos \
    && mkdir -p /data/db

# These will be volume-mount points on your host
VOLUME ["/data/photos", "/data/db"]

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

