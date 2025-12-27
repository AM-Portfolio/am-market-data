# Global Agent & LLM Guidelines

**Role**: Senior Architect & Lead Engineer
**Objective**: Build scalable, maintainable, and platform-agnostic systems.

This document establishes the **universal** rules and coding standards. These apply to **all** projects.

> **Note**: Workspace-specific rules (in .agent/rules/ and .agent/workflows/) override these defaults but should inherit the core philosophy.

## 1. Architectural Patterns (The "Pattern Recognition")
Before writing code, identify the module's role:
- **API Module** (*-api):
    - **Role**: The **Interface**. Pure contracts.
    - **Contains**: Controllers, OpenAPI Models, Interface Definitions.
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

## 2. Operational Excellence & Logging
**Principle**: "Logs are for machines, then humans."
- **Platform-Independent Standard**:
    - **Format**: JSON (Structured Logging) in Production. Text in Local.
    - **Correlation**: Every request MUST have 	race_id and span_id propagated across boundaries (HTTP headers, Kafka headers).
    - **Levels**:
        - ERROR: Wake up the on-call (Data loss, Service crash).
        - WARN: Degradation (Retries, Rate limits).
        - INFO: Auditable events (Startup, Shutdown, Key Business Txn).
        - DEBUG: Developer context.
- **Implementation Mapping**:
    - **Java**: SLF4J + MDC.
    - **Python**: Structlog / Standard Logging + ContextVars.
    - **Dart**: Logger + Zone.

## 3. SDK & Client Automation
**Principle**: "Never write a client manually."
- **Source of Truth**: The *-api module (OpenAPI Spec).
- **Automation Pipeline**:
    1.  **Build API**: Generate openapi.json (via Tests/Build).
    2.  **Generate Client**: openapi-generator builds the SDK.
    3.  **Publish**: SDK is versioned matching the API.
- **Platform Rules**:
    - **Java SDK**: Native HttpClient or Retrofit.
    - **Python SDK**: Pydantic models + httpx/
equests.
    - **Flutter SDK**: Dio + Freezed/JsonSerializable.

## 4. Performance & Scalability
**Principle**: "Async by default for I/O."
- **Database**:
    - No N+1 Queries.
    - Read/Write splitting where possible.
- **Caching**:
    - **L1 (Local)**: Caffeine/Guava for static config.
    - **L2 (Distributed)**: Redis for shared session/data.
    - **Pattern**: Cache-Aside or Write-Through.
- **Resilience**:
    - All external calls must have **Timeouts** and **Retries** (Exponential Backoff).
    - Use Circuit Breakers for failing downstream services.

## 5. Development Workflow
- **Test-First**: Define the Test (Contract) before the Implementation.
- **Boy Scout Rule**: Leave the file cleaner than you found it.
- **Documentation**: Explain *Why*, not *What* (Code explains *What*).

## 6. Schedulers & Background Jobs
**Principle**: "Decouple Execution from Trigger."
- **Isolation**: Schedulers belong in a dedicated *-scheduler module. They should NOT pollute the API or Core modules.
- **Idempotency**: All jobs must be safe to retry.
- **Distributed Locking**: Use locking (e.g., ShedLock with Redis) to prevent concurrent execution in clustered environments.
- **Observability**: Log Job Start, Success, and Failure with a unique Job ID.

## 7. External Integrations (The "Anti-Corruption Layer")
**Principle**: "Decouple Service from Provider."
- **Strict Isolation**: Integrations (e.g., Scrapers, External APIs) MUST live in *-scraper or *-external-api.
- **Golden Rule**: Use the **Anti-Corruption Layer (ACL)** pattern.
    - External modules fetch data -> Convert to Internal DTOs (*-common) -> Return to Service.
    - The *-service module MUST be completely decoupled. It should define an **Interface** that the External Module implements.
    - The Service Model never "knows" about provider specifics (e.g., "NSE_EQ") unless it's part of the Internal Domain.

## 8. Test Data & Versioning Strategy
- **Centralized Testing**: Do not scatter Postman collections or large JSON dumps across the repo.
- **Dedicated Module**: Use a specific module (e.g., *-versions) to store:
    - Postman Collections.
    - Sample Data / Mock Responses for testing.
    - API Versioning documentation.
- **Repo Hygiene**: Do NOT use the repository for temporary generation artifacts. Keep the repo clean.
