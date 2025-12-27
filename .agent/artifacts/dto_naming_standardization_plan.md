# DTO Naming Standardization Plan

## 🎯 Objective
Establish a consistent, versioned naming convention for all DTOs in `market-data-common` that will automatically propagate to all generated SDKs (Java, Python, Flutter).

## 📋 Current Issues
Based on the screenshot and analysis:
- ❌ Inconsistent naming: `historical_data.dart`, `historical_data_request.dart`, `historical_data_response_v1.dart`
- ❌ Some files have version suffixes (`_v1`), others don't
- ❌ No clear pattern for Request/Response DTOs
- ❌ Generated SDK files don't follow a uniform convention

## ✅ Proposed Naming Convention

### Pattern Structure
```
{EntityName}{Type}V{Version}
```

**Components:**
- `{EntityName}`: PascalCase entity name (e.g., `HistoricalData`, `MarketData`, `Security`)
- `{Type}`: One of: `Request`, `Response`, `DTO`, `Event` (optional for simple DTOs)
- `V{Version}`: Version number (e.g., `V1`, `V2`)

### Examples
```java
// Requests
HistoricalDataRequestV1.java
SecuritySearchRequestV1.java
QuotesRequestV1.java
BrokerageCalculationRequestV1.java

// Responses
HistoricalDataResponseV1.java
SecuritySearchResponseV1.java
QuotesResponseV1.java
BrokerageCalculationResponseV1.java

// DTOs (Data Transfer Objects - for nested/shared models)
OHLCQuoteV1.java
SecurityDTOV1.java
IndexMetadataV1.java

// Events
MarketDataEventV1.java
PriceUpdateEventV1.java
```

## 📁 Current Files to Rename

### In `market-data-common/src/main/java/com/am/marketdata/common/model/`

| Current Name | New Name | Type |
|--------------|----------|------|
| `SecurityDTO.java` | `SecurityDTOV1.java` | DTO |
| `SecuritySearchRequest.java` | `SecuritySearchRequestV1.java` | Request |
| `OHLCQuote.java` | `OHLCQuoteV1.java` | DTO |
| `NSEIndex.java` | `NSEIndexDTOV1.java` | DTO |
| `NSEIndicesResponse.java` | `NSEIndicesResponseV1.java` | Response |
| `NSEStockInsidicesData.java` | `NSEStockIndicesDataV1.java` | DTO (also fix typo) |
| `NseETF.java` | `NSEETFDtoV1.java` | DTO |
| `NseETFResponse.java` | `NSEETFResponseV1.java` | Response |
| `TimeFrame.java` | `TimeFrameV1.java` | Enum/DTO |
| `Instrument.java` | `InstrumentDTOV1.java` | DTO |

### Additional Files Needed (Based on Controllers)

```java
// Market Data Controller
MarketDataRequestV1.java
MarketDataResponseV1.java
HistoricalDataRequestV1.java
HistoricalDataResponseV1.java
QuotesRequestV1.java
QuotesResponseV1.java

// Brokerage Calculator
BrokerageCalculationRequestV1.java
BrokerageCalculationResponseV1.java

// Margin Calculator
MarginCalculationRequestV1.java
MarginCalculationResponseV1.java

// Market Index
MarketIndexRequestV1.java
MarketIndexResponseV1.java

// Stock Indices
StockIndicesRequestV1.java
StockIndicesResponseV1.java

// Market Analytics
MarketAnalyticsRequestV1.java
MarketAnalyticsResponseV1.java

// Polling
PollingRequestV1.java
PollingResponseV1.java
```

## 🔧 Implementation Steps

### Phase 1: Rename Existing DTOs (High Priority)
1. ✅ Rename all existing DTO files in `market-data-common`
2. ✅ Update all import statements across the project
3. ✅ Update controller method signatures
4. ✅ Update service implementations
5. ✅ Run full build to verify no breakages

### Phase 2: Create Missing DTOs
1. ✅ Create properly named Request/Response DTOs for each controller endpoint
2. ✅ Ensure all DTOs have proper JavaDoc with version info
3. ✅ Add `@Schema` annotations for OpenAPI documentation

### Phase 3: SDK Generation Configuration
1. ✅ Update OpenAPI generator config to preserve naming
2. ✅ Regenerate all SDKs (Java, Python, Flutter)
3. ✅ Verify consistent naming across all SDKs

### Phase 4: Documentation
1. ✅ Update API documentation
2. ✅ Create versioning guidelines document
3. ✅ Update README with naming conventions

## 📝 Naming Rules

### DO's ✅
- ✅ Always include version suffix (`V1`, `V2`, etc.)
- ✅ Use PascalCase for class names
- ✅ Use descriptive, domain-specific names
- ✅ Suffix with `Request` for input DTOs
- ✅ Suffix with `Response` for output DTOs
- ✅ Suffix with `DTO` for shared/nested models
- ✅ Suffix with `Event` for event-driven models

### DON'Ts ❌
- ❌ Don't use generic names like `Data`, `Info`, `Model`
- ❌ Don't omit version suffixes
- ❌ Don't mix naming conventions
- ❌ Don't use abbreviations unless widely understood (OK: `DTO`, `NSE`, `OHLC`)

## 🔄 Version Migration Strategy

When creating V2 of a DTO:
1. Keep V1 intact (don't delete)
2. Create new V2 class
3. Mark V1 as `@Deprecated` with migration notes
4. Update controllers to use V2
5. Maintain V1 endpoints for backward compatibility

Example:
```java
/**
 * @deprecated Use {@link HistoricalDataRequestV2} instead.
 * This version will be removed in release 3.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class HistoricalDataRequestV1 {
    // ...
}
```

## 📊 Impact Analysis

### Files Affected
- **Common Module**: ~10-15 files to rename
- **API Module**: ~8 controllers to update
- **Service Module**: ~8 service implementations to update
- **Test Files**: ~20 test files to update

### SDK Impact
- **Java SDK**: All model classes will be regenerated with new names
- **Python SDK**: All model classes will be regenerated with new names
- **Flutter SDK**: All model classes will be regenerated with new names

## ✅ Success Criteria
1. ✅ All DTOs follow `{Entity}{Type}V{Version}` pattern
2. ✅ Zero compilation errors after renaming
3. ✅ All tests pass
4. ✅ SDKs generate with consistent naming
5. ✅ OpenAPI spec reflects new naming
6. ✅ Documentation updated

## 🚀 Execution Timeline
- **Phase 1**: 2-3 hours (Rename existing + fix imports)
- **Phase 2**: 1-2 hours (Create missing DTOs)
- **Phase 3**: 30 minutes (Regenerate SDKs)
- **Phase 4**: 1 hour (Documentation)

**Total Estimated Time**: 4-6 hours

## 📌 Next Steps
1. Get approval for naming convention
2. Create backup branch
3. Execute Phase 1 (rename existing)
4. Run full test suite
5. Execute remaining phases
6. Generate final SDKs
7. Update documentation

---

**Status**: 📋 **READY FOR IMPLEMENTATION**  
**Priority**: 🔴 **HIGH** (Affects SDK generation quality)  
**Risk Level**: 🟡 **MEDIUM** (Requires careful refactoring)
