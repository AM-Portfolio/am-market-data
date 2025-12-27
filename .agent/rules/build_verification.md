---
trigger: always_on
glob: "**/*"
description: "Strict critical path rules for building, testing, and declaring task completion"
---
# Build & Verification Strategy (CRITICAL)

## 1. Zero-Tolerance Compilation
- **No Broken Builds**: You are forbidden from "finishing" a turn with compilation errors.
- **Syntax Check First**: Run `mvn compile -DskipTests` immediately after code edits to catch syntax errors early.

## 2. Mandatory Verification Protocol (The "Definition of Done")
- **Tests MUST Pass**: You cannot declare a task "Completed" unless relevant Unit Tests (UTs) are passing.
    - *Exception*: If you are explicitly asked to ignore tests by the user (rare).
- **Write Missing Tests**: If you implement a feature and no tests exist, **YOU MUST WRITE THEM**. Do not rely on manual assumptions.
- **Verify Logic**: Even if the code "looks correct" or "should work", you must prove it with a test execution.
    - **Command**: `mvn test -pl <module_name> -am`

## 3. Efficient Build Commands
- **NO Generic Clean Installs**: **NEVER** run `mvn clean install` blindly. It is slow.
    - **Preferred**: `mvn test -pl <module_name> -am` (Targeted testing) or `mvn compile` (Fast syntax check).
    - **Install**: Only use `install` if updating a shared library (`common`, `api`) that other local modules depend on.
