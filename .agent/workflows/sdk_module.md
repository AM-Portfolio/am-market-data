---
description: Build and test the SDK module
---
# SDK Module Workflow

This workflow handles operations on the **market-data-sdk** module.

## Keywords
When you say: `sdk`, `sdk module`, `client`, `java client`, `python client`, `flutter client`

## Common Operations

1. **Generate All SDKs**
   // turbo
   ```powershell
   ./market-data-sdk/generate_sdks.ps1
   ```

2. **Build SDK Module**
   // turbo
   ```bash
   mvn compile -pl market-data-sdk
   ```

3. **Test SDK Module**
   // turbo
   ```bash
   mvn test -pl market-data-sdk
   ```
