# Final Architecture - Service and Provider Separation

![Final Architecture Diagram](images/final_architecture_diagram_v2.png)

## Architecture Diagram (Complete with Scheduler + Provider Isolation)

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
│                LAYER 1: COMMON (Shared Language - Pure POJOs)            │
├─────────────────────────────────────────────────────────────────────────┤
│  market-data-common                                                      │
│  • DTOs & Models (v1/, v2/)                                             │
│  • OHLCQuote, TimeFrame, HistoricalDataRequest                          │
│  • Enums, Constants, Exceptions                                         │
│  • ZERO Dependencies (Pure Java)                                        │
└─────────────────────────────────────────────────────────────────────────┘
         ▲              ▲              ▲              ▲              ▲
         │              │              │              │              │
    ┌────┴────┐    ┌────┴────┐   ┌────┴────┐   ┌────┴────┐   ┌────┴────┐


┌─────────────────────────────────────────────────────────────────────────┐
│          LAYER 2: ENTRY POINTS & ISOLATED PROVIDER                       │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────┐  ┌─────────────┐  ┌────────────┐  ╔══════════════════════╗
│market-data-  │  │market-data- │  │market-data-│  ║ market-data-provider ║
│   api        │  │ scheduler   │  │  parser    │  ║   (ISOLATED)         ║
│  (PUBLIC)    │  │ (INTERNAL)⏰│  │            │  ║                      ║
│              │  │             │  │            │  ║ ┌──────────────────┐ ║
│ Controllers  │  │Cron Jobs:   │  │ OpenAPI    │  ║ │MarketDataProvider│ ║
│  (v1/)       │  │• Price Fetch│  │  Parser    │  ║ │   (Interface)    │ ║
│ REST         │  │  (1min)     │  │            │  ║ └──────────────────┘ ║
│ Endpoints    │  │• Redis      │  │   SDK      │  ║                      ║
│              │  │  Cleanup    │  │Generators  │  ║ Implementations:     ║
│              │  │  (Daily)    │  │            │  ║ ┌──────────────────┐ ║
│Entry:        │  │• Cache      │  │            │  ║ │UpstoxProvider    │ ║
│ Web Users ──►│  │  Warmup     │  │            │  ║ │ • UpstoxSDK      │ ║
└──────┬───────┘  │             │  │            │  ║ │ • API Client     │ ║
       │          │             │  │            │  ║ │ • Mapper         │ ║
       │          │             │  │            │  ║ └──────────────────┘ ║
       │          │             │  │            │  ║ ┌──────────────────┐ ║
       │          └──────┬──────┘  │            │  ║ │ZerodhaProvider   │ ║
       │                 │         │            │  ║ │ • ZerodhaSDK     │ ║
       │                 │         │            │  ║ │ • API Client     │ ║
       │                 │         │            │  ║ │ • Mapper         │ ║
       │                 │         └─────┬──────┘  ║ └──────────────────┘ ║
       │                 │               │         ║                      ║
       │                 │               │         ║ NO SERVICE CODE      ║
       │                 │               │         ║ ONLY PROVIDER LOGIC  ║
       └─────────┬───────┴───────────────┘         ╚═══════════════╪══════╝
                 │                                                  │
                 │                                         Interface Only
                 │                                         (No Direct Access)
                 ▼                                                  :
                                                                    :
┌────────────────────────────────────────────────────────────────────:─────┐
│      LAYER 3: SERVICE (Business Logic ONLY - NO Provider Code!)    :     │
├───────────────────────────────────────────────────────────────────────────┤
│  market-data-service                                                      │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │ PURE BUSINESS LOGIC & ORCHESTRATION                                 │ │
│  │                                                                      │ │
│  │ Service Implementations:                                            │ │
│  │ ┌──────────────────────────────────────────────────────────────┐   │ │
│  │ │ MarketDataFetchServiceImpl                                   │   │ │
│  │ │  • Validates input                                           │   │ │
│  │ │  • Applies business rules                                    │   │ │
│  │ │  • Calls provider.getQuotes() ──────────────────────────────────►: │
│  │ │  • Post-processes data                                       │   : │
│  │ │  • Caches results                                            │   : │
│  │ └──────────────────────────────────────────────────────────────┘   : │
│  │                                                                      : │
│  │ ┌──────────────────────────────────────────────────────────────┐   : │
│  │ │ MarketDataPollingServiceImpl                                 │   : │
│  │ │  • Manages WebSocket connections                             │   : │
│  │ │  • Uses provider.streamQuotes() ────────────────────────────────►: │
│  │ │  • Broadcasts to clients                                     │   : │
│  │ └──────────────────────────────────────────────────────────────┘   : │
│  │                                                                      : │
│  │ ┌──────────────────────────────────────────────────────────────┐   : │
│  │ │ BrokerageCalculatorServiceImpl                               │   : │
│  │ │  • Calculates brokerage fees                                 │   : │
│  │ │  • Applies tax rules                                         │   : │
│  │ │  • Business logic ONLY (no provider calls)                   │   : │
│  │ └──────────────────────────────────────────────────────────────┘   : │
│  │                                                                      │ │
│  │ Depends on:                                                          │ │
│  │ • api (interfaces)                                                   │ │
│  │ • common (DTOs)                                                      │ │
│  │ • provider (INTERFACE ONLY - no implementation dependency)          │ │
│  │                                                                      │ │
│  │ DOES NOT CONTAIN:                                                    │ │
│  │ ✗ Upstox SDK calls                                                   │ │
│  │ ✗ Zerodha SDK calls                                                  │ │
│  │ ✗ Provider-specific code                                             │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────┬───────────────────────────┘
                                                │
                                                ▼

┌─────────────────────────────────────────────────────────────────────────┐
│            LAYER 4: INFRASTRUCTURE (Runtime & Cache)                     │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────┐              ┌──────────────────────────┐
│ market-data-app             │              │ Redis Cache (L2)         │
│ (Spring Boot Container)     │              │                          │
│                             │              │ • Market Data Cache      │
│ Wires Together:             │              │ • Session Data           │
│ • API                       │ ◄──Cleanup───┤ • Instrument Cache       │
│ • Service                   │    Jobs      │                          │
│ • Provider (runtime inject) │              │ Accessed by:             │
│ • Scheduler                 │              │ • Service (read/write)   │
│                             │              │ • Scheduler (cleanup)    │
│ Configuration:              │              │                          │
│ @Bean                       │              └──────────────────────────┘
│ MarketDataProvider provider │
│   → UpstoxProvider          │
│   (or ZerodhaProvider)      │
└─────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────┐
│                 LAYER 5: SDK OUTPUT (Generated Clients)                  │
└─────────────────────────────────────────────────────────────────────────┘

  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
  │ market-data-sdk │     │ market-data-sdk │     │ market-data-sdk │
  │    -python      │     │   -flutter      │     │     -java       │
  ├─────────────────┤     ├─────────────────┤     ├─────────────────┤
  │ • Pydantic      │     │ • Freezed       │     │ • POJOs         │
  │ • Type-safe     │     │ • Type-safe     │     │ • Type-safe     │
  └────────▲────────┘     └────────▲────────┘     └────────▲────────┘
           │                       │                       │
           └───────────────────────┴───────────────────────┘
                                   │
                            [SDK Users Access]
```

## KEY ARCHITECTURAL CHANGE ⚠️

### Before (WRONG):
```
market-data-service/
├── service/
│   ├── MarketDataFetchServiceImpl.java      ← Business logic
│   ├── UpstoxApiService.java                ← Provider SDK code ❌
│   ├── UpstoxInstrumentService.java         ← Provider SDK code ❌
│   └── ZerodhaInstrumentService.java        ← Provider SDK code ❌
```

### After (CORRECT):
```
market-data-service/
├── service/
│   ├── MarketDataFetchServiceImpl.java      ← ONLY business logic ✅
│   ├── MarketDataPollingServiceImpl.java    ← ONLY business logic ✅
│   └── BrokerageCalculatorServiceImpl.java  ← ONLY business logic ✅

market-data-provider/
├── provider/
│   ├── MarketDataProvider.java              ← Interface
│   ├── upstox/
│   │   ├── UpstoxMarketDataProvider.java    ← Implementation ✅
│   │   ├── UpstoxApiClient.java             ← SDK wrapper ✅
│   │   ├── UpstoxInstrumentService.java     ← Provider logic ✅
│   │   └── mapper/
│   │       └── UpstoxToCommonMapper.java    ← Converts to DTOs ✅
│   └── zerodha/
│       ├── ZerodhaMarketDataProvider.java   ← Implementation ✅
│       └── ...
```

## Module Responsibilities (Crystal Clear)

### market-data-common
**Role**: Shared language  
**Contains**: DTOs, Models, Enums, Constants  
**Dependencies**: ZERO  
**NO**: Business logic, Provider code, API code

### market-data-api
**Role**: REST API interface layer  
**Contains**: Controllers, Service interfaces (contracts)  
**Dependencies**: common ONLY  
**NO**: Service implementations, Provider code

### market-data-provider
**Role**: Provider SDK implementations (ISOLATED)  
**Contains**:
- ✅ MarketDataProvider interface
- ✅ Upstox SDK integration
- ✅ Zerodha SDK integration
- ✅ External API clients
- ✅ Mappers (External models → Internal DTOs)

**Dependencies**: common ONLY  
**NO**: Business logic, Service code

### market-data-service
**Role**: Business logic & orchestration  
**Contains**:
- ✅ Service implementations (business rules)
- ✅ Validation logic
- ✅ Orchestration (calls provider via interface)
- ✅ Post-processing
- ✅ Caching logic

**Dependencies**: api (interfaces), common, provider (INTERFACE only)  
**NO**: Provider SDK code, Direct API calls to Upstox/Zerodha

### market-data-scheduler
**Role**: Background jobs  
**Contains**: Cron jobs, Scheduled tasks  
**Dependencies**: service (calls business logic)  
**NO**: Direct provider calls

### market-data-parser
**Role**: SDK generation  
**Contains**: OpenAPI parsing, SDK generators  
**Dependencies**: common  

### market-data-app
**Role**: Spring Boot runtime container  
**Contains**: Configuration, Bean wiring  
**Dependencies**: ALL modules (runtime assembly)  

## Dependency Flow (ENFORCED)

```
common ─────────────────────────────────┐
   ▲                                     │
   │                                     │
   ├── api ────────────────────┐        │
   │      ▲                     │        │
   │      │                     │        │
   ├── provider ────────┐       │        │
   │      ▲             │       │        │
   │      │ (interface) │       │        │
   ├── service ─────────┴───────┴────────┘
   │      ▲                     
   │      │                     
   ├── scheduler ──────────────┐
   │                            │
   ├── parser                   │
   │                            │
   └── app ─────────────────────┴── (wires everything)
```

## Data Flow Examples

### Example 1: Service Uses Provider (Correct Way)

```java
// market-data-service module
@Service
public class MarketDataFetchServiceImpl implements MarketDataFetchService {
    
    private final MarketDataProvider provider;  // Interface injection
    
    @Override
    public Map<String, OHLCQuote> fetchQuotes(List<String> symbols) {
        // 1. Business logic: Validate
        validateSymbols(symbols);
        
        // 2. Call provider (doesn't know if it's Upstox or Zerodha!)
        Map<String, OHLCQuote> quotes = provider.getQuotes(symbols);
        
        // 3. Business logic: Post-process
        applyFilters(quotes);
        
        // 4. Business logic: Cache
        cacheResults(quotes);
        
        return quotes;
    }
    
    // NO Upstox SDK code here!
    // NO Zerodha SDK code here!
    // ONLY business logic!
}
```

```java
// market-data-provider module
@Component
@ConditionalOnProperty(name="provider.type", havingValue="upstox")
public class UpstoxMarketDataProvider implements MarketDataProvider {
    
    private final UpstoxApiClient upstoxClient;  // SDK wrapper
    private final UpstoxToCommonMapper mapper;
    
    @Override
    public Map<String, OHLCQuote> getQuotes(List<String> symbols) {
        // 1. Provider-specific: Call Upstox SDK
        List<UpstoxQuote> upstoxQuotes = upstoxClient.fetchQuotes(symbols);
        
        // 2. Provider-specific: Convert to our DTOs (Anti-Corruption!)
        return upstoxQuotes.stream()
            .collect(Collectors.toMap(
                UpstoxQuote::getSymbol,
                mapper::toOHLCQuote
            ));
    }
    
    // ALL Upstox-specific code stays here!
}
```

### Example 2: Scheduler Job

```java
// market-data-scheduler module
@Component
public class MarketDataScheduler {
    
    private final MarketDataFetchService service;  // Business logic
    
    @Scheduled(cron = "0 */1 * * * *")
    public void fetchMarketPrices() {
        // Calls business logic
        // Service internally uses provider
        // Scheduler doesn't know about Upstox/Zerodha!
        service.fetchQuotes(getActiveSymbols());
    }
}
```

## Benefits of This Separation

✅ **Clean Code**: Each module has ONE responsibility  
✅ **Easy Testing**: Mock provider interface in service tests  
✅ **Provider Swap**: Change Upstox → Zerodha with config change  
✅ **No Coupling**: Service never knows about provider SDKs  
✅ **Maintainability**: Find code easily (business vs provider)  
✅ **Scalability**: Each module can evolve independently  

## Final Module Structure

```
market-data-service/
└── src/main/java/com/am/marketdata/service/
    ├── impl/
    │   ├── MarketDataFetchServiceImpl.java
    │   ├── MarketDataPollingServiceImpl.java
    │   └── BrokerageCalculatorServiceImpl.java
    └── util/
        └── ValidationUtils.java

market-data-provider/
└── src/main/java/com/am/marketdata/provider/
    ├── MarketDataProvider.java (interface)
    ├── upstox/
    │   ├── UpstoxMarketDataProvider.java
    │   ├── UpstoxApiClient.java
    │   ├── UpstoxInstrumentService.java
    │   ├── model/
    │   │   ├── UpstoxQuote.java
    │   │   └── UpstoxInstrument.java
    │   └── mapper/
    │       └── UpstoxToCommonMapper.java
    └── zerodha/
        └── ZerodhaMarketDataProvider.java
```

This is the FINAL, CLEAN architecture! 🎯
