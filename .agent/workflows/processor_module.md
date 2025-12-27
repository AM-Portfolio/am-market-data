---
description: Build and test the Processor module
---
# Processor Module Workflow

This workflow handles operations on the **market-data-processor** module.

## Keywords
When you say: `processor`, `processor module`, `data processing`, `transformation`

## Common Operations

1. **Compile Processor Module**
   // turbo
   ```bash
   mvn compile -pl market-data-processor
   ```

2. **Test Processor Module**
   // turbo
   ```bash
   mvn test -pl market-data-processor -am
   ```

3. **Install Processor Module**
   // turbo
   ```bash
   mvn install -DskipTests -pl market-data-processor
   ```
