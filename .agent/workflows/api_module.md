---
description: Build and test the API module
---
# API Module Workflow

This workflow handles operations on the **market-data-api** module.

## Keywords
When you say: `api`, `api module`, `interface`, `controller`, `rest api`

## Common Operations

1. **Compile API Module**
   // turbo
   ```bash
   mvn compile -pl market-data-api
   ```

2. **Test API Module**
   // turbo
   ```bash
   mvn test -pl market-data-api -am
   ```

3. **Install API Module**
   // turbo
   ```bash
   mvn install -DskipTests -pl market-data-api
   ```

4. **Generate OpenAPI Spec**
   // turbo
   ```bash
   mvn test -Dtest=OpenApiGeneratorTest -pl market-data-service
   ```
