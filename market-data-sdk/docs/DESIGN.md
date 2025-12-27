# SDK Architecture & Universal Schema Design

## Executive Summary
This document outlines the architecture for the `market-data-sdk`, focusing on a **Universal Schema** approach. The goal is to eliminate manual model parsing in client applications and ensure strict type safety across all supported languages (Java, Python, Dart).

## The Core Problem
Currently, SDKs or API clients often receive generic `Map<String, Object>` or untyped JSON. This forces developers to:
1.  Guess the data structure.
2.  Write manual parsers (error-prone).
3.  Maintain these parsers whenever the API changes.

## The Solution: Universal Parser via OpenAPI

We adopt a **Schema-First** (or Code-First with Schema Generation) strategy.

### 1. The Source of Truth (Backend)
The `market-data-service` defines the Contract.
- **DTOs**: Defined in `market-data-common` (e.g., `OHLCRequest`, `MarketDataResponse`).
- **Controllers**: Return these DTOs explicitly.
- **Schema Generation**: `SpringDoc` (Swagger) automatically introspects the Java code and produces a standard `openapi.json` (OpenAPI v3) specification.

### 2. The Universal Parser (Code Generation)
Instead of writing a custom "Universal Parser", we utilize the **OpenAPI Generator** ecosystem. This tool acts as the bridge, converting the `openapi.json` schema into native code for each target language.

**What is generated?**
- **Models**: Native classes (POJOs, Pydantic Models, Dart Classes) that strictly match the API.
- **Parsers**: JSON serialization/deserialization logic is built-in to these generated classes.
- **Clients**: HTTP Clients (Retrofit, requests, Dio) pre-configured to call the endpoints.

### 3. Data Flow
[ API Service ] --(generates)--> [ openapi.json ] --(feeds)--> [ OpenAPI Generator ]
                                                                        |
                                                 ------------------------------------------------
                                                 |                      |                       |
                                          [ SDK Python ]           [ SDK Dart ]           [ SDK Java ]
                                          (Pydantic)             (Freezed/Json)           (POJOs)

## Implementation Details

### A. Backend Configuration
- **Module**: `market-data-service`
- **Dependency**: `springdoc-openapi-starter-webmvc-ui`
- **Config**: `OpenApiConfig.java` to define Info block (Title, Version).

### B. Automation Script (`generate_sdks.ps1` / `.sh`)
A script will be provided to:
1.  Fetch the latest `openapi.json` (e.g., from `localhost:8080/v3/api-docs` or a build artifact).
2.  Run `openapi-generator-cli` for each language.
    - **Python**: Generates a pip-installable package.
    - **Dart**: Generates a Flutter-compatible package.
    - **Java**: Generates a Maven-compatible library.

## Strict Rules
1.  **No Manual Models**: Developers should NOT write data class files in the SDK manually. They must be generated.
2.  **DTO Hygiene**: API DTOs must be clean. Avoid circular references. Use Swagger annotations (`@Schema`) if validation details are needed.
3.  **Versioning**: API versioning should be reflected in the spec (e.g., `/v1/market-data`).

## Benefits
- **Zero Parsing Errors**: The generated code handles types perfectly.
- **Instant Updates**: Add a field to the Java DTO -> Run Script -> All SDKs have the new field.
- **Documentation**: The SDKs come with Javadoc/Docstrings derived from the API comments.
