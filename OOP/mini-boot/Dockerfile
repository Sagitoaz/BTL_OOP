# ==========================================
# DOCKERFILE FOR MINI-BOOT SERVER
# Deploy to Render.com
# ==========================================

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build

# Set working directory
WORKDIR /app

# Copy Maven files first (better caching)
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Download dependencies (will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application with maven-shade-plugin
RUN mvn clean package -DskipTests -B

# List target directory to verify JAR file (for debugging)
RUN ls -la /app/target/

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install dumb-init and wget for health check
RUN apk add --no-cache dumb-init wget

# Create app user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage (maven-shade creates this)
COPY --from=build /app/target/mini-boot-*.jar app.jar

# Verify the JAR has main manifest
RUN jar tf app.jar | grep -i manifest || echo "Warning: No manifest found"

# Copy application properties example (will use env vars at runtime)
COPY application.properties.example ./application.properties.example

# Change ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port (Render will set this dynamically)
EXPOSE 8080

# Note: Health check is disabled because Render uses its own health check mechanism
# and the port is dynamic (set via PORT env var at runtime)

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Run the application
# Note: PORT environment variable will be read by ServerMain.java
CMD ["java", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", \
     "app.jar"]
