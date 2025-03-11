# Builder stage
FROM eclipse-temurin:17-jdk-jammy AS builder

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy POM files first for better layer caching
COPY pom.xml .
COPY market-data-common/pom.xml market-data-common/
COPY market-data-kafka/pom.xml market-data-kafka/
COPY market-data-scraper/pom.xml market-data-scraper/
COPY market-data-service/pom.xml market-data-service/

# Download dependencies (this layer will be cached)
RUN mvn dependency:go-offline -B

# Copy source code
COPY market-data-common/src market-data-common/src
COPY market-data-kafka/src market-data-kafka/src
COPY market-data-scraper/src market-data-scraper/src
COPY market-data-service/src market-data-service/src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Install curl for healthcheck
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set timezone
ENV TZ=Asia/Kolkata
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/market-data-service/target/*.jar app.jar

# Environment variables
ENV SPRING_PROFILES_ACTIVE=docker

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
