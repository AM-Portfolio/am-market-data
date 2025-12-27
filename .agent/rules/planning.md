---
trigger: always_on
glob: "**/*plan*"
description: "Implementation Planning and Verification Strategy"
---
# Implementation Planning

## 1. Plan Structure
Every task requires implementation_plan.md:
- **Problem**: Context.
- **Design/Diagrams**: Mermaid flows.
- **Changes**: File-level breakdown.
- **Verification**: SPECIFIC commands (mvn test -pl market-data-api, curl ...).

## 2. Verification Steps (Template)
- **Unit Tests**: mvn test -pl <module>
- **Integration**: mvn verify -DskipTests=false
- **Manual**: Check Swagger UI (http://localhost:8080/swagger-ui.html).
