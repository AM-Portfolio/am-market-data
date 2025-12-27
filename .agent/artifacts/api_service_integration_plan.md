# API Service Integration Plan

## Objective
Refactor all controllers in `market-data-api` to follow the interface-service pattern:
- Controllers should ONLY inject and call interface methods
- Interfaces should be defined in `market-data-api/src/main/java/com/am/marketdata/api/service/`
- Implementations should be in `market-data-service/src/main/java/com/am/marketdata/service/impl/`
- Implementations should delegate to the actual service layer in `market-data-service`

## Completed Services

### ✅ MarketDataFetchService
- **Interface**: `MarketDataFetchService.java` in API module
- **Implementation**: `MarketDataFetchServiceImpl.java` in Service module
- **Controller**: `MarketDataController.java` - Uses interface
- **Delegates to**: `MarketDataService.java`

### ✅ BrokerageCalculatorApiService
- **Interface**: `BrokerageCalculatorApiService.java` in API module
- **Implementation**: `BrokerageCalculatorApiServiceImpl.java` in Service module
- **Controller**: `BrokerageCalculatorController.java` - Uses interface
- **Delegates to**: `BrokerageCalculatorService.java`

### ✅ MarginCalculatorApiService
- **Interface**: `MarginCalculatorApiService.java` in API module
- **Implementation**: `MarginCalculatorApiServiceImpl.java` in Service module
- **Controller**: `MarginCalculatorController.java` - Uses interface
- **Delegates to**: `MarginCalculatorService.java`

### ✅ SecurityApiService
- **Interface**: `SecurityApiService.java` in API module
- **Implementation**: `SecurityApiServiceImpl.java` in Service module
- **Controller**: `SecurityController.java` - Uses interface
- **Delegates to**: `SecurityService.java`

## Pending Services

### 🔄 MarketIndexApiService
- **Status**: Interface created, needs implementation
- **Controller**: `MarketIndexController.java` (currently disabled)
- **Service Layer**: Data comes from scraper module via `MarketDataProcessingService`
- **Action Required**:
  1. Create implementation that fetches from scraper/cache
  2. Enable and update controller

### 🔄 InstrumentApiService
- **Status**: Not started
- **Controller**: `InstrumentController.java` (currently disabled)
- **Service Layer**: Instrument data from providers
- **Action Required**:
  1. Define interface for instrument operations
  2. Create implementation
  3. Enable controller

### 🔄 MarketAnalyticsApiService
- **Status**: Not started
- **Controller**: `MarketAnalyticsController.java` (currently disabled)
- **Service Layer**: Analytics/aggregation logic
- **Action Required**:
  1. Define interface for analytics operations
  2. Create implementation
  3. Enable controller

### 🔄 MarketDataStreamApiService
- **Status**: Not started
- **Controller**: `MarketDataStreamController.java` (currently disabled)
- **Service Layer**: WebSocket/SSE streaming
- **Action Required**:
  1. Define interface for streaming operations
  2. Create implementation
  3. Enable controller

### 🔄 StockPortfolioApiService
- **Status**: Not started
- **Controller**: `StockPortfolioController.java` (currently disabled)
- **Service Layer**: Portfolio management
- **Action Required**:
  1. Define interface for portfolio operations
  2. Create implementation
  3. Enable controller

## Implementation Steps

For each pending service:

1. **Define DTO/Request/Response models** in `market-data-common` if needed
2. **Create API Interface** in `market-data-api/src/main/java/com/am/marketdata/api/service/`
   - Only method signatures
   - Return types should use common DTOs
3. **Create Implementation** in `market-data-service/src/main/java/com/am/marketdata/service/impl/`
   - Implements the API interface
   - Injects and delegates to actual service layer
   - Maps between service models and common DTOs
4. **Enable Controller** in `market-data-api/src/main/java/com/am/marketdata/api/controller/`
   - Inject the API interface (not the service directly)
   - Call interface methods
5. **Compile and Test**
   - Run `mvn compile -pl market-data-service -am`
   - Verify no circular dependencies

## Architecture Validation

### ✅ Correct Pattern
```
Controller -> API Interface -> Implementation -> Service Layer
(API module)  (API module)     (Service module)  (Service module)
```

### ❌ Incorrect Pattern (Avoid)
```
Controller -> Service Layer (Direct injection - WRONG!)
```

## Next Actions
1. Implement MarketIndexApiService
2. Implement InstrumentApiService
3. Implement remaining services
4. Full integration test
