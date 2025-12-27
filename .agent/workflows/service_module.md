---
description: Build and test the Service module
---
# Service Module Workflow

This workflow handles operations on the **market-data-service** module.

## Keywords
When you say: `service`, `service module`, `business logic`, `implementation`, `main service`

## Common Operations

1. **Compile Service Module**
   // turbo
   ```bash
   mvn compile -pl market-data-service
   ```

2. **Test Service Module**
   // turbo
   ```bash
   mvn test -pl market-data-service -am
   ```

3. **Run Service Locally**
   // turbo
   ```bash
   mvn spring-boot:run -pl market-data-service
   ```

4. **Build Service Module**
   // turbo
   ```bash
   mvn clean install -DskipTests -pl market-data-service -am
   ```

5. **Verify Service Module**
   // turbo
   ```bash
   mvn verify -DskipTests -pl market-data-service
   ```
