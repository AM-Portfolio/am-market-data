---
description: "Efficient build and verification workflow to avoid slow clean installs"
---
# Efficient Build Process

This workflow ensures code quality without wasting time on unnecessary full builds.

1. **Syntax Check (Fast)**
   ```bash
   mvn compile -DskipTests
   ```
   *Run this first to catch compilation errors quickly.*

2. **Targeted Testing (Recommended)**
   ```bash
   mvn test -pl <module_name> -am
   ```
   *Run tests ONLY for the module you modified. Use `-am` (also make) to build dependencies if needed.*

3. **Integration Check (Only if needed)**
   ```bash
   mvn install -DskipTests -pl <module_name> -am
   ```
   *Use this ONLY if you modified a shared library that other modules need to see.*

4. **Full Verify (Pre-Commit)**
   ```bash
   mvn verify -DskipTests
   ```
   *Run this before finishing your task to ensure no regressions, but skip tests if you already ran targeted tests.*
