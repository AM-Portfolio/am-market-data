# Phase 0: Discovery Report

**Date**: 2025-12-27  
**Analysis**: Provider Code in market-data-service

## Summary

Found **20 provider-related files** in `market-data-service` that need to be moved to `market-data-provider`.

## Discovered Files

### Upstox-Related (9 files):
1. `com\am\marketdata\service\model\UpstoxInstrument.java` - Model
2. `com\am\marketdata\service\repo\UpstoxInstrumentRepository.java` - Repository
3. `com\am\marketdata\service\service\UpstoxInstrumentService.java` - Service
4. `com\am\marketdata\upstock\config\UpstoxConfig.java` - Config
5. `com\marketdata\config\UpstoxApiConfig.java` - Config
6. `com\marketdata\service\upstox\UpstoxApiService.java` - API Service
7. `com\marketdata\service\upstox\UpstoxIndexIdentifier.java` - Utility
8. `com\marketdata\service\upstox\UpstoxMarketDataProvider.java` - **Provider Implementation**
9. `com\marketdata\service\upstox\UpstoxSdkService.java` - SDK Wrapper

### Zerodha-Related (11 files):
1. `com\am\marketdata\service\model\ZerodhaInstrument.java` - Model
2. `com\am\marketdata\service\repo\ZerodhaInstrumentRepository.java` - Repository
3. `com\am\marketdata\service\service\ZerodhaInstrumentService.java` - Service
4. `com\marketdata\config\ZerodhaApiConfig.java` - Config
5. `com\marketdata\service\zerodha\ZerodhaApiException.java` - Exception
6. `com\marketdata\service\zerodha\ZerodhaApiService.java` - API Service
7. `com\marketdata\service\zerodha\ZerodhaMarketDataProvider.java` - **Provider Implementation**
8. `com\marketdata\service\zerodha\ZerodhaMarketDataScheduler.java` - Scheduler (might be business logic)
9. `com\marketdata\service\zerodha\event\ZerodhaOHLCEvent.java` - Event
10. `com\marketdata\service\zerodha\event\ZerodhaQuoteEvent.java` - Event
11. `com\marketdata\service\zerodha\event\ZerodhaTickEvent.java` - Event

### Provider Interface (1 file):
- `com\marketdata\common\MarketDataProvider.java` - **Interface definition**

## Key Findings

### 1. MarketDataProvider Interface EXISTS
**Location**: `market-data-service/src/main/java/com/marketdata/common/MarketDataProvider.java`

**Contract** (key methods):
- `void initialize()`
- `Map<String, OHLCQuote> getOHLC(List<String> symbols, TimeFrame timeFrame)`
- `HistoricalData getHistoricalData(...)`
- `List<Instrument> getAllInstruments()`
- `String getProviderName()`

**External Dependencies Noted**:
- `com.zerodhatech.models.Instrument` - Zerodha SDK (JAR)
- `com.zerodhatech.models.LTPQuote` - Zerodha SDK (JAR)
- `com.am.common.investment.model.historical.HistoricalData` - Project common
- `com.am.marketdata.common.model.OHLCQuote` - Project common

⚠️ **Issue**: Interface uses Zerodha-specific classes → Needs refactoring to use common DTOs

### 2. Package Structure Issues

**Multiple package prefixes found**:
- `com.am.marketdata.*` (new structure)
- `com.marketdata.*` (legacy structure)

**Action Required**: Consolidate to `com.am.marketdata.provider.*`

### 3. Classification

#### **MOVE to provider** (Pure provider code):
- All `*MarketDataProvider.java` implementations
- All `*ApiService.java` files
- All `*SdkService.java` files
- All `*Config.java` for providers
- All provider-specific models that aren't shared

#### **EVALUATE** (Might have business logic):
- `ZerodhaMarketDataScheduler.java` - Could be business scheduling
- `*InstrumentRepository.java` - Data access, might stay
- `*InstrumentService.java` - Might have business logic

#### **REFACTOR Interface**:
- Remove Zerodha-specific classes (`com.zerodhatech.models.*`)
- Use only common DTOs
- Move to provider module

## Phase 1 Execution Plan

Based on discoveries:

1. **Create provider module structure** ✅ (Already done)

2. **Copy and refactor interface**:
   - Copy `MarketDataProvider.java` to provider module
   - Replace Zerodha-specific classes with common DTOs
   - Update package to `com.am.marketdata.provider`

3. **Move provider implementations**:
   - Start with `UpstoxMarketDataProvider.java`
   - Then `ZerodhaMarketDataProvider.java`
   - Move all supporting files (ApiService, SdkService, Config)

4. **Handle models**:
   - `UpstoxInstrument.java` → Provider-specific model (move)
   - `ZerodhaInstrument.java` → Provider-specific model (move)

5. **Handle repositories**:
   - Analyze if they're provider-specific or business data
   - Decision pending after viewing files

## Next Steps

1. View `UpstoxMarketDataProvider.java` to understand implementation
2. View `ZerodhaMarketDataScheduler.java` to check if business logic
3. View repositories to determine if they should move
4. Start Phase 1 execution

## Dependencies to Check

Need to verify in pom.xml:
- Upstox SDK version
- Zerodha SDK version
- Spring Boot version compatibility
