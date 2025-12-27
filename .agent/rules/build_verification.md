---
trigger: always_on
glob: "**/*"
description: "Build Commands, Testing Protocols, and CI/CD"
---
# Build & Verification

## 1. Efficiency
- **Fast Build**: mvn clean compile -pl <module>
- **Fast Test**: mvn test -pl <module> -am (also builds dependencies)

## 2. Zero-Tolerance
- Do not check in broken code.
- Run mvn compile immediately after any refactor.

## 3. Test Data
- Use market-data-versions for Postman collections and static JSON data.
