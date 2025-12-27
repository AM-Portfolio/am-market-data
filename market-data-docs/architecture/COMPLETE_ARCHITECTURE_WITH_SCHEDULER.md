# Complete Architecture with Scheduler Layer

## Architecture Diagram (Enhanced)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          EXTERNAL USERS                                  │
└─────────────────────────────────────────────────────────────────────────┘
         │                                                      │
    [Web Users]                                          [SDK Users]
    (Browsers)                                      (External Clients)
         │                                                      │
         │ HTTP Requests                                       │ SDK Calls
         ▼                                                      ▼


┌─────────────────────────────────────────────────────────────────────────┐
│                     LAYER 1: COMMON (Shared Language)                    │
├─────────────────────────────────────────────────────────────────────────┤
│  market-data-common                                                      │
│  • DTOs & Models (v1/, v2/)                                             │
│  • OHLCQuote, TimeFrame, HistoricalDataRequest                          │
│  • ZERO Dependencies                                                     │
└─────────────────────────────────────────────────────────────────────────┘
         ▲                    ▲                    ▲                ▲
         │                    │                    │                │
    ┌────┴────┐          ┌────┴────┐         ┌────┴────┐      ┌────┴────┐


┌─────────────────────────────────────────────────────────────────────────┐
│             LAYER 2: ENTRY POINTS (Public & Internal)                    │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────────┐  ┌───────────────────┐  ┌──────────────┐
│ market-data-api  │  │market-data-       │  │market-data-  │  ╔═══════════╗
│   (PUBLIC)       │  │  scheduler        │  │  parser      │  ║ market-   ║
│ ┌──────────────┐ │  │   (INTERNAL)    ⏰│  │              │  ║  data-    ║
│ │Controllers   │ │  │ ┌───────────────┐ │  │ ┌──────────┐ │  ║ provider  ║
│ │  (v1/)       │ │  │ │Scheduled Jobs │ │  │ │ OpenAPI  │ │  ║           ║
│ │REST Endpoints│ │  │ │               │ │  │ │ Parser   │ │  ║ISOLATED   ║
│ └──────────────┘ │  │ │• Price Fetch  │ │  │ │          │ │  ║           ║
│                  │  │ │  (Every 1min) │ │  │ │   SDK    │ │  ║ NO DIRECT ║
│ Entry: Web Users │  │ │• Redis Cleanup│ │  │ │Generators│ │  ║  ACCESS   ║
└────────┬─────────┘  │ │  (Daily)      │ │  │ └──────────┘ │  ║           ║
         │            │ │• Cache Warmup │ │  │              │  ║ Interface ║
         │            │ │• Data Sync    │ │  │              │  ║   ONLY    ║
         │            │ └───────┬───────┘ │  └──────┬───────┘  ║           ║
         │            └─────────┼─────────┘         │          ╚═══════════╝
         │                      │                   │                 │
         │                      │                   │                 │
         └──────────┬───────────┴───────────────────┘                 │
                    │                                                  │
                    │                                         Interface Only
                    ▼                                         (Dotted Line)
                                                                       :
┌─────────────────────────────────────────────────────────────────────:───┐
│         LAYER 3: SERVICE (Orchestration & Business Logic)           :   │
├─────────────────────────────────────────────────────────────────────────┤
│  market-data-service                                                     │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │ Business Logic Layer                                              │  │
│  │                                                                    │  │
│  │ Services:                                                          │  │
│  │ • MarketDataFetchServiceImpl  ◄─────┐                            │  │
│  │ • MarketDataPollingServiceImpl       │                            │  │
│  │ • BrokerageCalculatorServiceImpl     │ Uses Provider Interface    │  │
│  │                                      │ (Abstraction Layer)        │  │
│  │ Receives from:                       └───────────────────────────────┤
│  │ 1. API (Web Requests)                                             :  │
│  │ 2. Scheduler (Background Jobs) ◄── Clock-driven                  :  │
│  │                                                                    :  │
│  │ Calls:                                                            :  │
│  │ → Provider (via interface) ─────────────────────────────────────►:  │
│  │ → Parser (feeds OpenAPI data)                                     │  │
│  │ → Redis (cache operations)                                        │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────────────────┘
                               │
                               ▼

┌─────────────────────────────────────────────────────────────────────────┐
│               LAYER 4: INFRASTRUCTURE (Runtime & Cache)                  │
└─────────────────────────────────────────────────────────────────────────┘

┌───────────────────────┐                    ┌────────────────────────────┐
│ market-data-app       │                    │ Redis Cache (L2)           │
│ (Spring Boot)         │                    │                            │
│ ┌───────────────────┐ │                    │ • Distributed Cache        │
│ │ Runtime Container │ │                    │ • Session Data             │
│ │                   │ │                    │ • Market Data Cache        │
│ │ Orchestrates:     │ │ ◄────Cleanup──────┤                            │
│ │ • Service         │ │      Jobs          │ Cleanup Jobs from:         │
│ │ • Scheduler       │ │                    │ • Scheduler (Daily)        │
│ │ • Provider        │ │                    │ • Service (On-demand)      │
│ └───────────────────┘ │                    │                            │
└───────────────────────┘                    └────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────┐
│                    LAYER 5: SDK OUTPUT (Generated Clients)               │
└─────────────────────────────────────────────────────────────────────────┘

    ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
    │ market-data-sdk │     │ market-data-sdk │     │ market-data-sdk │
    │     -python     │     │    -flutter     │     │     -java       │
    ├─────────────────┤     ├─────────────────┤     ├─────────────────┤
    │ • Pydantic      │     │ • Freezed       │     │ • POJOs         │
    │ • httpx Client  │     │ • Dio Client    │     │ • Native Client │
    │ • Type-safe     │     │ • Type-safe     │     │ • Type-safe     │
    └────────▲────────┘     └────────▲────────┘     └────────▲────────┘
             │                       │                       │
             └───────────────────────┴───────────────────────┘
                                     │
                              [SDK Users Access]
```

## Key Architectural Principles

### 1. **Scheduler Layer (NEW)**
- **Module**: `market-data-scheduler`
- **Purpose**: Background jobs and automated tasks
- **Access**: INTERNAL ONLY (not exposed to web)
- **Responsibilities**:
  - Market price fetching (cron: every 1 minute)
  - Redis cache cleanup (cron: daily)
  - Cache warm-up (on startup)
  - Data synchronization jobs
- **How it works**: Calls Service layer (same as API) → Service uses Provider

### 2. **Provider Isolation**
- **Critical**: Provider is NEVER accessed directly
- **Rule**: Only Service layer can call Provider (via interface)
- **Benefit**: Can swap providers (Upstox ↔ Zerodha) without touching API or Scheduler

### 3. **User Interaction Points**

#### Web Users:
```
Web Browser → API (market-data-api) → Service → Provider
                                    ↓
                                  Redis Cache
```

#### SDK Users:
```
External App → SDK Client → API (market-data-api) → Service → Provider
```

#### Scheduler (Internal):
```
Cron Job → Scheduler → Service → Provider
                    ↓
                  Redis (cleanup)
```

### 4. **Data Flow Examples**

**Example 1: Web User Fetches Market Data**
1. User opens browser → Hits `/api/v1/market-data/quotes`
2. API Controller receives request
3. Calls `MarketDataFetchService` (via interface)
4. Service calls `MarketDataProvider.getQuotes()` (interface)
5. Upstox/Zerodha implementation fetches data
6. Service caches in Redis
7. Returns to API → User

**Example 2: Scheduler Auto-Fetches Prices**
1. Cron triggers (every 1 minute)
2. Scheduler job calls `MarketDataFetchService`
3. Service calls `MarketDataProvider.getQuotes()`
4. Data fetched from Upstox/Zerodha
5. Service caches in Redis
6. Job completes (no HTTP response needed)

**Example 3: SDK User Makes Request**
1. External app uses Python SDK
2. SDK calls `/api/v1/market-data/historical`
3. API Controller → Service → Provider
4. Data returned to SDK
5. SDK deserializes to Pydantic models
6. Type-safe access in user's app

### 5. **Scheduler Jobs**

```java
// market-data-scheduler module

@Component
public class MarketDataScheduler {
    
    private final MarketDataFetchService marketDataService;
    private final RedisCacheManager cacheManager;
    
    // Fetch market prices every minute
    @Scheduled(cron = "0 */1 * * * *")
    public void fetchMarketPrices() {
        log.info("Scheduled job: Fetching market prices");
        List<String> symbols = getActiveSymbols();
        marketDataService.fetchQuotes(symbols);  // Uses Provider internally
        log.info("Completed: Fetched {} symbols", symbols.size());
    }
    
    // Clean up Redis cache daily at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupRedisCache() {
        log.info("Scheduled job: Redis cleanup");
        cacheManager.evictExpiredEntries();
        log.info("Completed: Redis cleanup");
    }
    
    // Warm up cache on application startup
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        log.info("Startup job: Cache warmup");
        marketDataService.warmupCache();
        log.info("Completed: Cache warmup");
    }
}
```

## Module Dependencies (Final)

```
market-data-common      → ZERO
market-data-provider    → common
market-data-api         → common
market-data-scheduler   → common, service (impl)
market-data-service     → api, common, provider (interface)
market-data-parser      → common
market-data-app         → service, api, scheduler, provider (runtime)
```

## Benefits of This Architecture

✅ **Clean Separation**: Web, Scheduled Jobs, SDK all go through Service  
✅ **Provider Isolation**: No direct access - easy to swap providers  
✅ **Background Jobs**: Scheduler handles automated tasks independently  
✅ **Caching**: Redis cleanup and management via Scheduler  
✅ **Type Safety**: SDKs provide strongly-typed clients  
✅ **Scalability**: Each layer can scale independently  
✅ **Maintainability**: Clear boundaries and responsibilities  
