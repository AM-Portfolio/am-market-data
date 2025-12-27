---
trigger: always_on
glob: "**/*sdk*"
description: "Guidelines for SDK development and API module interaction"
---
# SDK Development Guidelines

## 1. Architecture & Separation
- **Strict Separation**: SDKs must be distinct from the core API module. They are consumers of the API, not extensions of it.
- **Shared Commons**: If logic is duplicated or the API module's data structure is hard to consume, **refactor the API module first**. Extract shared DTOs/models into a common library (e.g., `market-data-common`) that both the API service and the SDKs can import.

## 2. Multi-Language Support
- **Mandatory Languages**: When implementing or updating SDK functionality, you must consider/create SDKs for:
    1. **Java**
    2. **Python**
    3. **Flutter (Dart)**
- **Consistency**: Naming conventions and method signatures should be as consistent as possible across languages, respecting each language's idioms.

## 3. Implementation Workflow
- **Refactor-First Approach**: Do not hack the SDK to work around API limitations. Fix the API contract or data models in the upstream module first.
- **Clean Contracts**: Ensure the API exposes data in a clean, strictly-typed format suitable for client generation.
