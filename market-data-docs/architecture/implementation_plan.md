# Implementation Plan - Universal SDK Architecture

## problem
Current SDKs rely on dynamic objects (`Map<String, Object>`) or manual parsing. This is error-prone and requires consumers to write their own parsers. The goal is to have a "Universal Parser" system where Models (DTOs) are defined once (in the API) and automatically propagated to all SDKs (Java, Python, Dart) with strong typing.

## Proposed Architecture: "Agile Universal Schema"
We will implement an **OpenAPI-centric** architecture.
1.  **Source of Truth**: The Java Code (`market-data-common` DTOs + `market-data-service` Controllers) is the source.
2.  **Universal Schema**: Using `SpringDoc`, we automatically generate a `openapi.json` (Swagger Spec) that describes every endpoint and data model.
3.  **Code Generation**: We use `OpenAPI Generator` to automatically build:
    *   **Python Client**: Complete with Pydantic models.
    *   **Dart Client**: Complete with JSON serialization (freezed/json_serializable).
    *   **Java Client**: Complete with Retrofit/Jersey/Native clients.

## Changes

### 1. Documentation & Rules
- Create `market-data-sdk/DESIGN.md` outlining the "Universal Parser" strategy.
- (Attempted) Update `.agent/rules/sdk.md` to mandate this approach.

### 2. Backend (Universal Schema Generator)
- **Module**: `market-data-api` (Lighter weight, contains Controllers)
- **Dependency**: Add `springdoc-openapi-starter-webmvc-ui` to `market-data-api/pom.xml`.
- **Test-Based Generation**: Implement `OpenApiGeneratorTest` in `market-data-api` to generate `openapi.json` during the `test` phase. This avoids the need to start the full application (DB connections, etc.).

### 3. SDK Automation (The "Universal Generation")
- **Script**: Update `generate_sdks.ps1` to:
    1. Run `mvn test -pl market-data-api` to generate the spec.
    2. Read `market-data-api/target/openapi.json`.
    3. Run `openapi-generator-cli` for all targets.

## Verification Plan
1.  **Manual**: Run `market-data-service`. Access `http://localhost:8080/swagger-ui.html` to confirm Schema exists and looks correct.
2.  **Automated**: Run `generate_sdks.ps1`. Check `market-data-sdk/market-data-sdk-python` and ensure Pydantic models exist.
