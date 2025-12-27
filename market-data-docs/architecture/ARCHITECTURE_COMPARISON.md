# Current vs. Proposed Architecture

## Current Architecture (PROBLEMS) 🔴

```
┌──────────────────────────────────────────────────────────┐
│ market-data-api (MIXED RESPONSIBILITIES)                 │
│ ┌────────────────────────────────────────────────────┐   │
│ │ ✅ Controllers                                     │   │
│ │ ❌ Service Implementations (SHOULD NOT BE HERE!)  │   │
│ │    - BrokerageCalculatorApiService                │   │
│ │    - MarketDataFetchServiceImpl                   │   │
│ │    - MarketDataPollingService                     │   │
│ └────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────┐
│ market-data-service (MIXED WITH PROVIDER CODE)           │
│ ┌────────────────────────────────────────────────────┐   │
│ │ ✅ Business Logic Services                        │   │
│ │ ❌ Upstox-specific code (SHOULD BE ISOLATED!)     │   │
│ │    - UpstoxApiService                             │   │
│ │    - UpstoxMarketDataProvider                     │   │
│ │    - UpstoxSdkService                             │   │
│ │    - UpstoxInstrument (models)                    │   │
│ │ ❌ Zerodha-specific code                          │   │
│ │    - ZerodhaInstrumentService                     │   │
│ └────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘

PROBLEMS:
1. Cannot switch providers easily
2. Service layer tightly coupled to Upstox SDK
3. API module has business logic (violates interface-only rule)
4. No API versioning
```

---

## Proposed Architecture (CLEAN) ✅

```
                     ┌─────────────────────────┐
                     │  market-data-common     │
                     │  ┌───────────────────┐  │
                     │  │ DTOs & Models     │  │
                     │  │ (Shared Language) │  │
                     │  │                   │  │
                     │  │ v1/               │  │
                     │  │ - OHLCQuote       │  │
                     │  │ - TimeFrame       │  │
                     │  │ - *Request        │  │
                     │  │ - *Response       │  │
                     │  │                   │  │
                     │  │ v2/ (future)      │  │
                     │  └───────────────────┘  │
                     │  ZERO dependencies      │
                     └─────────────────────────┘
                                ▲
                    ┌───────────┼───────────┐
                    │           │           │
        ┌───────────┴───┐      │      ┌────┴──────────────┐
        │               │      │      │                   │
┌───────▼──────────┐    │      │      │  ┌────────────────▼─────┐
│ market-data-api  │    │      │      │  │ market-data-provider │
│ ┌──────────────┐ │    │      │      │  │ ┌──────────────────┐ │
│ │ v1/          │ │    │      │      │  │ │ Interface:       │ │
│ │ Controllers  │ │    │      │      │  │ │ MarketDataProv.. │ │
│ │              │ │    │      │      │  │ └──────────────────┘ │
│ ├──────────────┤ │    │      │      │  │                     │ │
│ │ Service      │ │    │      │      │  │ Implementations:    │ │
│ │ Interfaces   │ │    │      │      │  │ ┌─────────────────┐ │ │
│ │ (contracts)  │ │    │      │      │  │ │ upstox/         │ │ │
│ └──────────────┘ │    │      │      │  │ │ - Provider      │ │ │
│                  │    │      │      │  │ │ - ApiClient     │ │ │
│ Depends:         │    │      │      │  │ │ - Mapper        │ │ │
│ - common only    │    │      │      │  │ └─────────────────┘ │ │
└──────────────────┘    │      │      │  │ ┌─────────────────┐ │ │
                        │      │      │  │ │ zerodha/        │ │ │
                        │      │      │  │ │ - Provider      │ │ │
                        │      │      │  │ └─────────────────┘ │ │
                        │      │      │  │                     │ │
                        │      │      │  │ Depends: common     │ │
                        │      │      │  └─────────────────────┘ │
                        │      │      │                          │
                        │ impl │      │ injects                  │
                        ▼      │      ▼                          │
        ┌────────────────────────────────────────────┐           │
        │  market-data-service                       │           │
        │  ┌──────────────────────────────────────┐  │           │
        │  │ Business Logic Layer                 │  │           │
        │  │                                      │  │           │
        │  │ Services (Implementations):          │  │           │
        │  │ - MarketDataFetchServiceImpl         │  │           │
        │  │ - MarketDataPollingServiceImpl       │  │           │
        │  │ - BrokerageCalculatorServiceImpl     │  │           │
        │  │                                      │  │           │
        │  │ Uses:                                │  │           │
        │  │   MarketDataProvider interface ─────┼──┼───────────┘
        │  │   (injected at runtime)              │  │
        │  │                                      │  │
        │  │ Does NOT know about:                 │  │
        │  │   - Upstox SDK                       │  │
        │  │   - Zerodha SDK                      │  │
        │  └──────────────────────────────────────┘  │
        │                                            │
        │ Depends: api + common + provider (iface)   │
        └────────────────────────────────────────────┘
```

---

## Key Differences

| Aspect | Current (Bad) | Proposed (Good) |
|--------|---------------|-----------------|
| **API Module** | Contains service implementations | ONLY controllers + interfaces |
| **Service Module** | Contains Upstox/Zerodha code | ONLY business logic |
| **Provider Code** | Mixed in service | Isolated in dedicated module |
| **Coupling** | Tight (knows about Upstox SDK) | Loose (uses interface) |
| **Testability** | Hard to mock Upstox | Easy (mock provider interface) |
| **Provider Switching** | Code changes required | Config change only |
| **API Versioning** | None | v1, v2, etc. |
| **SDK Generation** | Unpredictable | Clean, versioned |

---

## Data Flow Example

### Current (Problematic)
```
Controller 
  → calls UpstoxApiService directly
    → uses Upstox SDK
      → returns Upstox-specific model
        → manual conversion to OHLCQuote

Problem: Controller knows about Upstox!
```

### Proposed (Clean)
```
Controller (v1)
  → calls MarketDataFetchService (interface)
    → ServiceImpl uses MarketDataProvider (interface)
      → UpstoxMarketDataProvider (impl)
        → uses Upstox SDK internally
        → converts via UpstoxToCommonMapper
      → returns OHLCQuote (our model)
    → ServiceImpl adds business logic
  → returns v1 Response DTO

Benefits:
- Controller has NO idea about Upstox
- Service has NO idea about Upstox
- Provider can be swapped (Upstox → Zerodha) with zero code changes
```

---

## Anti-Corruption Layer Pattern

The `MarketDataProvider` interface is the **Anti-Corruption Layer**:

```java
// Service doesn't know WHO provides data
@Service
public class MarketDataFetchServiceImpl implements MarketDataFetchService {
    
    private final MarketDataProvider provider;  // Could be ANYONE
    
    public Map<String, OHLCQuote> fetchQuotes(List<String> symbols) {
        // Business logic
        validateSymbols(symbols);
        
        // Delegate to provider (we don't care if it's Upstox or Zerodha)
        Map<String, OHLCQuote> quotes = provider.getQuotes(symbols);
        
        // More business logic
        applyFilters(quotes);
        cacheResults(quotes);
        
        return quotes;
    }
}

// Provider implementations handle conversion
@Component
public class UpstoxMarketDataProvider implements MarketDataProvider {
    
    @Override
    public Map<String, OHLCQuote> getQuotes(List<String> symbols) {
        // Call Upstox SDK
        List<UpstoxQuote> upstoxQuotes = upstoxSdk.getQuotes(symbols);
        
        // Convert to OUR model (Anti-Corruption!)
        return upstoxQuotes.stream()
            .collect(Collectors.toMap(
                UpstoxQuote::getSymbol,
                UpstoxToCommonMapper::toOHLCQuote
            ));
    }
}
```

---

## Module Dependency Rules (Enforced)

```
✅ ALLOWED:
market-data-api        → market-data-common
market-data-provider   → market-data-common
market-data-service    → market-data-api, market-data-common, market-data-provider

❌ FORBIDDEN:
market-data-api        → market-data-service  (API should NOT know about service!)
market-data-api        → market-data-provider (API should NOT know about providers!)
market-data-common     → ANY internal module  (Common must be pure!)
market-data-service    → Upstox SDK directly  (Must go through provider!)
```

---

## Testing Benefits

### Current
```java
@Test
void testFetchQuotes() {
    // Problem: Have to mock Upstox SDK, which is in service layer!
    UpstoxApiService upstoxService = mock(UpstoxApiService.class);
    // Tight coupling to provider
}
```

### Proposed
```java
@Test
void testFetchQuotes() {
    // Easy: Mock the provider interface
    MarketDataProvider mockProvider = mock(MarketDataProvider.class);
    when(mockProvider.getQuotes(anyList()))
        .thenReturn(Map.of("NIFTY", buildOHLCQuote()));
    
    MarketDataFetchServiceImpl service = new MarketDataFetchServiceImpl(mockProvider);
    
    // Test business logic in isolation!
    Map<String, OHLCQuote> result = service.fetchQuotes(List.of("NIFTY"));
    
    // No knowledge of Upstox/Zerodha needed
}
```

---

## Summary

**Current**: Spaghetti 🍝
- API has services
- Service has provider code
- Everything knows about Upstox

**Proposed**: Clean Layers 🎂
- API = Interface (controllers + contracts)
- Service = Business Logic (no provider knowledge)
- Provider = Isolated implementations
- Common = Shared language

**Result**: Maintainable, testable, evolvable architecture! ✨
