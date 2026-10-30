# Multi-Stage Build Dockerfile for Hospital Emergency System
# Stage 1: Build Application with Maven & Eclipse Temurin JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven POM and dependency descriptors
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build package
COPY src ./src
COPY database ./database
RUN mvn clean package -DskipTests

# Stage 2: Runtime Image with JavaFX & X11 display support
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install X11 / GTK client libraries for JavaFX desktop rendering
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1-mesa-glx \
    libgtk-3-0 \
    libxxf86vm1 \
    libasound2 \
    && rm -rf /var/lib/apt/lists/*

# Copy built JAR artifact from build stage
COPY --from=build /app/target/hospital-emergency-system-1.0.0.jar app.jar
COPY config ./config

# Expose display environment variable for X11 forwarding
ENV DISPLAY=:0

ENTRYPOINT ["java", "-jar", "app.jar"]
