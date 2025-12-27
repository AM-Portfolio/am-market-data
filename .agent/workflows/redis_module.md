---
description: Build and test the Redis module
---
# Redis Module Workflow

This workflow handles operations on the **market-data-redis** module.

## Keywords
When you say: `redis`, `redis module`, `cache`, `caching layer`

## Common Operations

1. **Compile Redis Module**
   // turbo
   ```bash
   mvn compile -pl market-data-redis
   ```

2. **Test Redis Module**
   // turbo
   ```bash
   mvn test -pl market-data-redis -am
   ```

3. **Install Redis Module**
   // turbo
   ```bash
   mvn install -DskipTests -pl market-data-redis
   ```
