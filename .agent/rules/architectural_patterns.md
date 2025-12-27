---
trigger: always_on
glob: "**/*.java"
description: "Module Roles, Dependency Rules, and Code Structure for Market Data"
---
# Architectural Patterns (Market Data)

## 1. Module Roles
- **API Module** (*-api):
    - **Role**: The **Interface**. Pure contracts.
    - **Contains**: Controllers (@RestController), Swagger/OpenAPI Models (generated or defined).
    - **Dependencies**: Depends ONLY on *-common. **NEVER** depends on *-service.
    - **Output**: Source of Truth for SDKs (OpenAPI Spec).
- **Service Module** (*-service):
    - **Role**: The **Implementation**. Business logic & Data access.
    - **Contains**: Service Classes, Repositories, DB Entities.
    - **Dependencies**: Depends on *-api (to implement interfaces) and *-common.
- **Common Module** (*-common):
    - **Role**: The **Language**. Shared vocabulary.
    - **Contains**: DTOs, Enums, Constants, Utils.
    - **Dependencies**: **ZERO** internal project dependencies. Pure POJOs/Pydantic models.
- **SDK Module** (*-sdk):
    - **Role**: The **Client**. Auto-generated libraries.
    - **Contains**: Java, Python, Flutter clients generated from OpenAPI.
    - **Dependencies**: Independent of the core service logic.

## 2. Dependency Rules
- **Strict Layering**: API -> Common <- Service. Service -> API.
- **No Circular Deps**: Common never sees API or Service.
- **External Isolation**: All external API calls (Upstox, Kite, etc.) must go through dedicated scraper/provider modules or be strictly isolated in an infrastructure package within service, implementing domain interfaces.

## 3. Code Standards
- **Minimality**: Small, focused files.
- **Modularity**: Loose coupling.
- **Clean Code**: Self-documenting.
