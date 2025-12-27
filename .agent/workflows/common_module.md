---
description: Build and test the Common module
---
# Common Module Workflow

This workflow handles operations on the **market-data-common** module.

## Keywords
When you say: common, common module, dto, shared, models, enums, utils

## Common Operations

1. **Compile Common Module**
   // turbo
   `ash
   mvn compile -pl market-data-common
   `

2. **Test Common Module**
   // turbo
   `ash
   mvn test -pl market-data-common
   `

3. **Install Common Module**
   // turbo
   `ash
   mvn clean install -pl market-data-common
   `
   *Use this when you modify DTOs/Enums that other modules depend on.*
