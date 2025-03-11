# Use Eclipse Temurin as base image for Java 17
FROM eclipse-temurin:17-jdk-jammy as builder

# Set working directory
WORKDIR /app

# Install Maven and create .m2 directory
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /root/.m2

# Copy Maven settings template and configure it
COPY settings.template.xml /root/.m2/settings.xml

# Copy parent pom first for layer caching
COPY pom.xml ./

# Copy module pom files
COPY market-data-api/pom.xml ./market-data-api/
COPY market-data-app/pom.xml ./market-data-app/
COPY market-data-common/pom.xml ./market-data-common/
COPY market-data-kafka/pom.xml ./market-data-kafka/
COPY market-data-scraper/pom.xml ./market-data-scraper/
COPY market-data-service/pom.xml ./market-data-service/

# Download dependencies (this layer will be cached)
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN
ENV GITHUB_USERNAME=${GITHUB_USERNAME}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}
RUN mvn dependency:go-offline -B \
    -s /root/.m2/settings.xml

# Copy source files
COPY market-data-api/src ./market-data-api/src/
COPY market-data-app/src ./market-data-app/src/
COPY market-data-common/src ./market-data-common/src/
COPY market-data-kafka/src ./market-data-kafka/src/
COPY market-data-scraper/src ./market-data-scraper/src/
COPY market-data-service/src ./market-data-service/src/

# Build the application
RUN mvn clean package -DskipTests \
    -s /root/.m2/settings.xml

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Install curl for healthcheck and set timezone
RUN apt-get update && \
    apt-get install -y curl tzdata && \
    ln -fs /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    dpkg-reconfigure -f noninteractive tzdata && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built artifacts from builder stage
COPY --from=builder /app/market-data-app/target/*.jar ./app.jar

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set timezone
ENV TZ=Asia/Kolkata

# Expose application port
EXPOSE 8080

# Run the application with proper memory settings
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "/app/app.jar"]
