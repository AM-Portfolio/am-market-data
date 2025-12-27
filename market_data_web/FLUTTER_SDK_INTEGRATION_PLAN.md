# Flutter SDK Integration: Strategic Architectural Vision

## 1. The Strategy: Clean Architecture & Anti-Corruption Layer (ACL)
As a Senior Architect, our primary goal is **decoupling**. We will not allow the auto-generated SDK to dictate our UI structure. Instead, we implement a professional three-tier architecture that isolates volatility and ensures the UI remains stable and high-performing.

### The BluePrint
![Clean Architecture](file:///a:/InfraCode/AM-Portfolio/am-market-data/docs/architecture/clean_architecture.png)

## 2. Dynamic Integration Pillars

### A. The Anti-Corruption Layer (ACL)
The **Mapping Logic** resides strictly in the `RepositoryImpl`. 
- **UI:** Operates on Domain Models (e.g., `IndexData`).
- **Data Layer:** Operates on SDK Models (e.g., `StockIndicesMarketDataV1`).
- **The Bridge:** The Repository translates these models. If the backend changes a field name, only the Mapper changes. The UI is oblivious and safe.

### B. Reactive Data Flow
We unify our communication strategy. The UI doesn't care if data is being polled via HTTP or pushed via WebSocket.

![Data Flow Strategy](file:///a:/InfraCode/AM-Portfolio/am-market-data/docs/architecture/data_flow_repository.png)

### C. Unified Service Layer
The `MarketDataSdkService` is the single source of truth for communication.
1. **HTTP Client:** Initial state fetching using the 8 generated API clients.
2. **WebSocket Client:** Real-time delta updates (previously in `stream_service.dart`).
3. **Smart Orchestration:** Logic to merge the live delta into the base state before delivering it to the UI.

## 3. Implementation Roadmap

### Phase 0: SDK Module WebSocket Integration (STAGING)
The foundation of a robust platform is a powerful SDK. We must first integrate WebSocket capabilities directly into the `market_data_client` module.

- **Objective:** The SDK must handle its own real-time communication.
- **Tasks:**
  - Implement `MarketDataWebSocketClient` in the SDK.
  - Handle connection lifecycle (Connect, Disconnect, Reconnect).
  - Define Event Models within the SDK package.
  - Export unified streaming API.

### Phase 1: Domain Definition (Stable Models)
Once the SDK is capable, we define our **Domain Layer** in the Flutter app. These models are optimized for the user experience, not for the database or API.

### Phase 2: The Infrastructure Core
Implement the `MarketDataSdkService` (HTTP + WS consolidated). This layer handles retries, backoffs, and connection health.

### Phase 3: The Mapper Implementation
Develop the high-performance mappers that convert SDK DTOs into Domain Entities.

### Phase 4: Reactive Repository
Expose data as `Streams` or `Future` models that the UI can consume via standard state management.

## 4. Why this approach?
- **Efficiency:** Drastic reduction in boilerplate code for JSON parsing.
- **Maintainability:** Full control over the service layer.
- **Longevity:** A change in the SDK version (V1 to V2) requires **zero** changes in the presentation layer.

---
*Created by: Senior Architect Architect*
*Location:* [docs/architecture/](file:///a:/InfraCode/AM-Portfolio/am-market-data/docs/architecture/)
