---
description: Build and test the Scheduler module
---
# Scheduler Module Workflow

This workflow handles operations on the **market-data-scheduler** module.

## Keywords
When you say: `scheduler`, `scheduler module`, `jobs`, `scheduled tasks`, `cron`

## Common Operations

1. **Compile Scheduler Module**
   // turbo
   ```bash
   mvn compile -pl market-data-scheduler
   ```

2. **Test Scheduler Module**
   // turbo
   ```bash
   mvn test -pl market-data-scheduler -am
   ```

3. **Install Scheduler Module**
   // turbo
   ```bash
   mvn install -DskipTests -pl market-data-scheduler
   ```
