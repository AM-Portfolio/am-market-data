# Architecture Refactoring Plan - Isolating Providers and Enforcing Module Boundaries

## Executive Summary
This plan addresses critical architectural violations and establishes proper separation of concerns by:
1. **Extracting provider-specific code** (Upstox, Zerodha) into a dedicated `market-data-provider` module
2. **Removing business logic from API module** (services should NOT be in API)
3. **Enforcing strict module boundaries** according to the architectural rules
4. **Introducing API versioning** for all requests and responses

---

## Current Architecture Issues

### 🔴 Critical Violations Found

#### 1. **API Module Contains Implementation Logic**
**Location**: `market-data-api/src/main/java/com/am/marketdata/api/service/`

**Files that MUST be moved**:
```
- BrokerageCalculatorApiService.java
- MarginCalculatorApiService.java
- MarketAnalyticsService.java
- MarketDataFetchService.java (interface is OK, implementation is NOT)
- MarketDataPollingService.java
- StockIndicesService.java
- impl/MarketDataFetchServiceImpl.java
```

**Problem**: API module should ONLY contain:
- Controllers (`@RestController`)
- Interface definitions (contracts)
- NOT service implementations

**Impact**: Violates the "API as Interface" principle. API module becomes coupled to business logic.

---

#### 2. **Provider Code Mixed in Service Module**
**Location**: `market-data-service/src/main/java/com/marketdata/service/upstox/`

**Provider-specific files currently in service**:
```
Upstox Provider:
- UpstoxApiService.java
- UpstoxMarketDataProvider.java
- UpstoxSdkService.java
- UpstoxIndexIdentifier.java
- UpstoxInstrumentService.java
- UpstoxInstrument.java (model)
- UpstoxInstrumentRepository.java
- UpstoxConfig.java
- UpstoxApiConfig.java

Zerodha Provider:
- ZerodhaInstrumentService.java
- (potentially more)
```

**Problem**: Service module contains provider-specific implementation details.

**Impact**: 
- Cannot switch providers easily
- Tight coupling to Upstox/Zerodha SDKs
- Violates the Anti-Corruption Layer pattern

---

#### 3. **No API Versioning**
**Current**: All endpoints are unversioned (e.g., `/api/market-data`)

**Required**: Version all APIs (e.g., `/api/v1/market-data`)

**Impact**: Cannot evolve API without breaking clients

---

## Proposed Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                      market-data-common                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  DTOs, Models, Enums, Constants                          │  │
│  │  - OHLCQuote, TimeFrame, HistoricalDataRequest          │  │
│  │  - MarketDataException (domain exceptions)              │  │
│  │  ZERO dependencies                                       │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
                              ▲
                              │ depends on
    ┌─────────────────────────┼─────────────────────────┐
    │                         │                         │
┌───┴────────────────────┐   │   ┌────────────────────┴────┐
│   market-data-api      │   │   │  market-data-provider   │
│  ┌──────────────────┐  │   │   │  ┌────────────────────┐ │
│  │ Controllers      │  │   │   │  │ Provider Interface │ │
│  │ - v1/...        │  │   │   │  │ MarketDataProvider │ │
│  │ - v2/...        │  │   │   │  │                    │ │
│  ├──────────────────┤  │   │   │  └────────────────────┘ │
│  │ Service          │  │   │   │  Implementations:       │
│  │ Interfaces       │  │   │   │  - UpstoxProvider      │
│  │ (contracts only) │  │   │   │  - ZerodhaProvider     │
│  └──────────────────┘  │   │   │  - MockProvider        │
│  Depends: common only  │   │   │  Depends: common       │
└────────────┬───────────┘   │   └────────────┬───────────┘
             │               │                │
             │ implements    │                │
             ▼               │                │
┌────────────────────────────┴────────────────┴───────────────┐
│                  market-data-service                         │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Service Layer (Business Logic)                        │  │
│  │ - MarketDataFetchServiceImpl                          │  │
│  │ - MarketDataPollingServiceImpl                        │  │
│  │ - BrokerageCalculatorServiceImpl                      │  │
│  │                                                        │  │
│  │ Uses: MarketDataProvider interface                    │  │
│  │ (Injected at runtime via Spring)                      │  │
│  └───────────────────────────────────────────────────────┘  │
│  Depends: api + common + provider (interface)               │
└─────────────────────────────────────────────────────────────┘
```

---

## Complete Architecture with SDK Generation

![Complete Architecture Flow](.agent/images/complete_architecture_flow.png)

### Architecture Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CORE MODULES                                  │
└─────────────────────────────────────────────────────────────────────┘

                     ┌─────────────────────────┐
                     │  market-data-common     │
                     │  ┌───────────────────┐  │
                     │  │ DTOs & Models     │  │
                     │  │ v1/, v2/          │  │
                     │  │ OHLCQuote         │  │
                     │  │ TimeFrame         │  │
                     │  └───────────────────┘  │
                     └────────────┬────────────┘
                                  │
            ┌─────────────────────┼─────────────────────┐
            │                     │                     │
    ┌───────▼──────────┐  ┌──────▼────────┐   ┌───────▼──────────┐
    │ market-data-api  │  │ market-data-  │   │ market-data-     │
    │                  │  │   provider    │   │   service        │
    │ • Controllers    │  │               │   │                  │
    │ • Interfaces     │  │ • Interface   │   │ • Business Logic │
    │ • v1/, v2/       │  │ • Upstox Impl │   │ • Uses Provider  │
    └──────────────────┘  │ • Zerodha Impl│   │   Interface      │
                          └───────────────┘   └──────────────────┘
                                  │                     │
                                  └──────────┬──────────┘
                                             │
                                    ┌────────▼─────────┐
                                    │ market-data-app  │
                                    │ (Spring Boot)    │
                                    └────────┬─────────┘
                                             │
┌─────────────────────────────────────────────────────────────────────┐
│                     SDK GENERATION PIPELINE                          │
└─────────────────────────────────────────────────────────────────────┘
                                             │
                                    ┌────────▼─────────────┐
                                    │ OpenApiGeneratorTest │
                                    │ (Generates Spec)     │
                                    └────────┬─────────────┘
                                             │
                                    ┌────────▼─────────┐
                                    │  openapi.json    │
                                    │  (API Spec)      │
                                    └────────┬─────────┘
                                             │
                                    ┌────────▼──────────┐
                                    │ market-data-parser│
                                    │                   │
                                    │ • OpenAPI Parser  │
                                    │ • Schema Validator│
                                    │ • Model Extractor │
                                    └────────┬──────────┘
                                             │
                      ┌──────────────────────┼──────────────────────┐
                      │                      │                      │
            ┌─────────▼─────────┐  ┌────────▼────────┐  ┌─────────▼─────────┐
            │ Python Generator  │  │ Dart Generator  │  │  Java Generator   │
            │                   │  │                 │  │                   │
            │ • Pydantic Models │  │ • Freezed       │  │ • POJOs           │
            │ • httpx Client    │  │ • Dio Client    │  │ • Native Client   │
            └─────────┬─────────┘  └────────┬────────┘  └─────────┬─────────┘
                      │                     │                      │
                      │                     │                      │
        ┌─────────────▼──────┐  ┌──────────▼───────┐  ┌──────────▼──────────┐
        │ market-data-sdk-   │  │ market-data-sdk- │  │ market-data-sdk-    │
        │     python/        │  │    flutter/      │  │      java/          │
        │                    │  │                  │  │                     │
        │ • models/          │  │ • lib/model/     │  │ • src/.../model/    │
        │ • api/             │  │ • lib/api/       │  │ • src/.../api/      │
        │ • setup.py         │  │ • pubspec.yaml   │  │ • pom.xml           │
        └────────────────────┘  └──────────────────┘  └─────────────────────┘
```

---

## New Modules

### 1. market-data-provider

**Purpose**: Isolate ALL provider-specific implementations (Upstox, Zerodha, etc.)

### 2. market-data-parser

**Purpose**: Parse OpenAPI JSON and orchestrate SDK generation for all target languages

### 3. market-data-sdk (Parent Module)

**Purpose**: Container for all generated SDK submodules

---

## Module: market-data-parser

### Structure
```
market-data-parser/
├── src/main/java/com/am/marketdata/parser/
│   ├── OpenApiParser.java              # Parses openapi.json
│   ├── SchemaValidator.java            # Validates spec completeness
│   ├── ModelExtractor.java             # Extracts all model definitions
│   │
│   ├── generator/
│   │   ├── SdkGenerator.java           # Base interface
│   │   ├── PythonSdkGenerator.java     # Python-specific generation
│   │   ├── DartSdkGenerator.java       # Dart/Flutter generation
│   │   └── JavaSdkGenerator.java       # Java SDK generation
│   │
│   ├── config/
│   │   ├── GeneratorConfig.java        # SDK generation configuration
│   │   └── TemplateConfig.java         # Mustache templates config
│   │
│   └── model/
│       ├── ParsedSchema.java           # Parsed OpenAPI structure
│       ├── ApiEndpoint.java            # Endpoint definition
│       └── ModelDefinition.java        # Model/DTO definition
│
├── src/main/resources/
│   ├── templates/                      # Mustache templates (optional)
│   │   ├── python/
│   │   ├── dart/
│   │   └── java/
│   └── generator-config.yml            # Configuration for each language
│
├── src/test/java/
│   └── ParserTests.java
│
└── pom.xml
    Dependencies:
    - swagger-parser (for parsing openapi.json)
    - openapi-generator (for SDK generation)
    - jackson (for JSON processing)
```

### Key Features

#### 1. **OpenAPI Parser**
```java
@Component
public class OpenApiParser {
    
    /**
     * Parses openapi.json and extracts all information needed for SDK generation
     */
    public ParsedSchema parse(Path openApiJsonPath) {
        // Read and parse openapi.json
        OpenAPI openAPI = new OpenAPIParser()
            .readLocation(openApiJsonPath.toString(), null, null)
            .getOpenAPI();
        
        // Extract components
        ParsedSchema schema = new ParsedSchema();
        schema.setInfo(openAPI.getInfo());
        schema.setPaths(extractEndpoints(openAPI.getPaths()));
        schema.setModels(extractModels(openAPI.getComponents()));
        
        return schema;
    }
    
    private List<ModelDefinition> extractModels(Components components) {
        // Extract ALL models from schemas
        return components.getSchemas().entrySet().stream()
            .map(entry -> ModelDefinition.builder()
                .name(entry.getKey())
                .properties(extractProperties(entry.getValue()))
                .build())
            .collect(Collectors.toList());
    }
}
```

#### 2. **Schema Validator**
```java
@Component
public class SchemaValidator {
    
    /**
     * Validates that openapi.json contains all expected models
     */
    public ValidationResult validate(ParsedSchema schema) {
        List<String> missingModels = new ArrayList<>();
        
        // Check for critical models
        String[] requiredModels = {
            "OHLCQuote", "TimeFrame", "HistoricalDataRequest",
            "QuotesRequest", "MarginCalculationRequest"
        };
        
        for (String model : requiredModels) {
            if (!schema.hasModel(model)) {
                missingModels.add(model);
            }
        }
        
        return ValidationResult.builder()
            .valid(missingModels.isEmpty())
            .missingModels(missingModels)
            .totalModels(schema.getModels().size())
            .build();
    }
}
```

#### 3. **SDK Generators**

**Python Generator**:
```java
@Component
public class PythonSdkGenerator implements SdkGenerator {
    
    @Override
    public void generate(ParsedSchema schema, Path outputDir) {
        GeneratorConfig config = GeneratorConfig.builder()
            .generatorName("python")
            .inputSpec(schema.getSourcePath())
            .outputDir(outputDir.toString())
            .additionalProperties(Map.of(
                "packageName", "market_data_client",
                "projectName", "market-data-client",
                "packageVersion", schema.getVersion()
            ))
            .build();
        
        // Use OpenAPI Generator
        OpenAPIGenerator.generate(config);
        
        // Post-processing: Add custom methods, fix imports, etc.
        postProcess(outputDir);
    }
    
    private void postProcess(Path outputDir) {
        // Custom post-processing for Python SDK
        // - Add __init__.py files
        // - Add helper methods
        // - Format with black
    }
}
```

**Dart/Flutter Generator**:
```java
@Component
public class DartSdkGenerator implements SdkGenerator {
    
    @Override
    public void generate(ParsedSchema schema, Path outputDir) {
        GeneratorConfig config = GeneratorConfig.builder()
            .generatorName("dart")
            .inputSpec(schema.getSourcePath())
            .outputDir(outputDir.toString())
            .additionalProperties(Map.of(
                "pubName", "market_data_client",
                "pubVersion", schema.getVersion(),
                "pubDescription", "Market Data SDK for Flutter"
            ))
            .build();
        
        OpenAPIGenerator.generate(config);
        postProcess(outputDir);
    }
    
    private void postProcess(Path outputDir) {
        // Custom post-processing for Dart SDK
        // - Run dart format
        // - Generate barrel exports
        // - Add Flutter-specific helpers
    }
}
```

**Java Generator**:
```java
@Component
public class JavaSdkGenerator implements SdkGenerator {
    
    @Override
    public void generate(ParsedSchema schema, Path outputDir) {
        GeneratorConfig config = GeneratorConfig.builder()
            .generatorName("java")
            .inputSpec(schema.getSourcePath())
            .outputDir(outputDir.toString())
            .additionalProperties(Map.of(
                "apiPackage", "com.am.marketdata.client.api",
                "modelPackage", "com.am.marketdata.client.model",
                "library", "native",
                "dateLibrary", "java8"
            ))
            .build();
        
        OpenAPIGenerator.generate(config);
        postProcess(outputDir);
    }
}
```

---

## Module: market-data-sdk

### Structure
```
market-data-sdk/
├── generate_sdks.ps1                   # PowerShell script
├── generate_sdks.sh                    # Bash script
├── DESIGN.md                           # SDK architecture docs
│
├── market-data-sdk-python/             # Generated Python SDK
│   ├── market_data_client/
│   │   ├── __init__.py
│   │   ├── models/
│   │   │   ├── ohlc_quote.py
│   │   │   ├── time_frame.py
│   │   │   ├── historical_data_request.py
│   │   │   └── ...
│   │   ├── api/
│   │   │   ├── market_data_api.py
│   │   │   └── ...
│   │   └── client.py
│   ├── setup.py
│   ├── requirements.txt
│   └── README.md
│
├── market-data-sdk-flutter/            # Generated Dart/Flutter SDK
│   ├── lib/
│   │   ├── model/
│   │   │   ├── ohlc_quote.dart
│   │   │   ├── time_frame.dart
│   │   │   ├── historical_data_request.dart
│   │   │   └── ...
│   │   ├── api/
│   │   │   ├── market_data_api.dart
│   │   │   └── ...
│   │   └── market_data_client.dart
│   ├── pubspec.yaml
│   ├── analysis_options.yaml
│   └── README.md
│
└── market-data-sdk-java/               # Generated Java SDK
    ├── src/main/java/com/am/marketdata/client/
    │   ├── model/
    │   │   ├── OHLCQuote.java
    │   │   ├── TimeFrame.java
    │   │   ├── HistoricalDataRequest.java
    │   │   └── ...
    │   ├── api/
    │   │   ├── MarketDataApi.java
    │   │   └── ...
    │   └── ApiClient.java
    ├── pom.xml
    └── README.md
```

### SDK Generation Workflow

```
1. Developer makes API changes
   ↓
2. Run: mvn test -Dtest=OpenApiGeneratorTest
   ↓ (generates openapi.json)
3. Run: ./generate_sdks.ps1
   ↓
4. Parser Module:
   - Parses openapi.json
   - Validates all models present
   - Extracts endpoints and models
   ↓
5. Generates SDKs:
   - Python SDK (with Pydantic models)
   - Dart SDK (with Freezed/JSON serialization)
   - Java SDK (with POJOs)
   ↓
6. Post-processing:
   - Format code
   - Add documentation
   - Run SDK tests
   ↓
7. Publish:
   - Python: pip install market-data-client
   - Dart: pub.dev or local path
   - Java: Maven repository
```

### Expected SDK Usage

**Python**:
```python
from market_data_client import MarketDataApi, ApiClient
from market_data_client.models import HistoricalDataRequest, TimeFrame

# Initialize client
client = ApiClient(base_url="http://localhost:8080")
api = MarketDataApi(client)

# Use strongly-typed models
request = HistoricalDataRequest(
    symbols="NIFTY 50",
    from_date="2024-01-01",
    to_date="2024-12-31",
    interval=TimeFrame.MINUTE
)

# Get data
quotes = api.get_historical_data(request)

# Type-safe access
for symbol, data in quotes.items():
    print(f"{symbol}: Open={data.open}, Close={data.close}")
```

**Dart/Flutter**:
```dart
import 'package:market_data_client/market_data_client.dart';

// Initialize
final api = MarketDataApi(basePath: 'http://localhost:8080');

// Use strongly-typed models
final request = HistoricalDataRequest(
  symbols: 'NIFTY 50',
  from: '2024-01-01',
  to: '2024-12-31',
  interval: TimeFrame.MINUTE,
);

// Get data
final quotes = await api.getHistoricalData(request);

// Type-safe access
quotes.forEach((symbol, data) {
  print('$symbol: Open=${data.open}, Close=${data.close}');
});
```

**Java**:
```java
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.api.MarketDataApi;
import com.am.marketdata.client.model.*;

// Initialize
ApiClient client = new ApiClient();
client.setBasePath("http://localhost:8080");
MarketDataApi api = new MarketDataApi(client);

// Use strongly-typed models
HistoricalDataRequest request = HistoricalDataRequest.builder()
    .symbols("NIFTY 50")
    .from("2024-01-01")
    .to("2024-12-31")
    .interval(TimeFrame.MINUTE)
    .build();

// Get data
Map<String, OHLCQuote> quotes = api.getHistoricalData(request);

// Type-safe access
quotes.forEach((symbol, data) -> {
    System.out.printf("%s: Open=%f, Close=%f%n", 
        symbol, data.getOpen(), data.getClose());
});
```

---

## New Module: market-data-provider

### Purpose
Isolate ALL provider-specific implementations (Upstox, Zerodha, etc.)

### Structure
```
market-data-provider/
├── src/main/java/com/am/marketdata/provider/
│   ├── MarketDataProvider.java         # Interface (Anti-Corruption Layer)
│   ├── ProviderType.java                # Enum: UPSTOX, ZERODHA
│   │
│   ├── upstox/
│   │   ├── UpstoxMarketDataProvider.java    # Implements MarketDataProvider
│   │   ├── UpstoxApiClient.java
│   │   ├── UpstoxInstrumentService.java
│   │   ├── UpstoxConfig.java
│   │   ├── model/
│   │   │   ├── UpstoxInstrument.java
│   │   │   └── UpstoxQuote.java       # External API models
│   │   └── mapper/
│   │       └── UpstoxToCommonMapper.java  # Converts Upstox → OHLCQuote
│   │
│   ├── zerodha/
│   │   ├── ZerodhaMarketDataProvider.java
│   │   └── ...
│   │
│   └── factory/
│       └── MarketDataProviderFactory.java  # Returns provider based on config
│
└── pom.xml
    Dependencies:
    - market-data-common (for DTOs)
    - Upstox SDK (external)
    - Zerodha SDK (external)
```

### Key Interface: MarketDataProvider

```java
package com.am.marketdata.provider;

import com.am.marketdata.common.model.OHLCQuote;
import com.marketdata.common.dto.HistoricalDataRequest;
import java.util.Map;

/**
 * Anti-Corruption Layer for Market Data Providers
 * 
 * This interface ensures service layer is NEVER coupled to provider specifics.
 * All provider implementations MUST convert their native formats to our internal DTOs.
 */
public interface MarketDataProvider {
    
    /**
     * Get current quotes for symbols
     * @param symbols List of symbols
     * @return Map of symbol → OHLCQuote (our internal model)
     */
    Map<String, OHLCQuote> getQuotes(List<String> symbols);
    
    /**
     * Get historical data
     * @param request Our internal request DTO
     * @return Map of symbol → List<OHLCQuote>
     */
    Map<String, List<OHLCQuote>> getHistoricalData(HistoricalDataRequest request);
    
    /**
     * Get login URL for OAuth
     */
    String getLoginUrl();
    
    /**
     * Generate session from auth code
     */
    String generateSession(String authCode);
    
    // ... other methods
}
```

---

## Refactoring Steps

### Phase 1: Create market-data-provider Module

#### Step 1.1: Create Module Structure
```bash
mkdir market-data-provider
mkdir -p market-data-provider/src/main/java/com/am/marketdata/provider
mkdir -p market-data-provider/src/test/java/com/am/marketdata/provider
```

#### Step 1.2: Create pom.xml
```xml
<artifactId>market-data-provider</artifactId>
<name>Market Data Provider</name>
<description>Provider implementations (Upstox, Zerodha, etc.)</description>

<dependencies>
    <dependency>
        <groupId>com.marketdata</groupId>
        <artifactId>market-data-common</artifactId>
    </dependency>
    <!-- Upstox SDK -->
    <!-- Zerodha SDK -->
</dependencies>
```

#### Step 1.3: Create Provider Interface
Create `MarketDataProvider.java` (interface shown above)

#### Step 1.4: Move Upstox Code
**From**: `market-data-service/src/main/java/com/marketdata/service/upstox/`
**To**: `market-data-provider/src/main/java/com/am/marketdata/provider/upstox/`

**Files to move**:
- UpstoxApiService.java → UpstoxMarketDataProvider.java (rename & implement interface)
- UpstoxSdkService.java → UpstoxApiClient.java
- UpstoxIndexIdentifier.java
- UpstoxConfig.java
- UpstoxApiConfig.java

**From**: `market-data-service/src/main/java/com/am/marketdata/service/`
**To**: `market-data-provider/src/main/java/com/am/marketdata/provider/upstox/`

**Files to move**:
- UpstoxInstrumentService.java
- UpstoxInstrument.java (model)
- UpstoxInstrumentRepository.java

#### Step 1.5: Create Mapper
Create `UpstoxToCommonMapper.java`:
```java
public class UpstoxToCommonMapper {
    /**
     * Converts Upstox native quote to our internal OHLCQuote
     */
    public static OHLCQuote toOHLCQuote(UpstoxQuote upstoxQuote) {
        return OHLCQuote.builder()
                .symbol(upstoxQuote.getSymbol())
                .open(upstoxQuote.getOpen())
                .high(upstoxQuote.getHigh())
                .low(upstoxQuote.getLow())
                .close(upstoxQuote.getClose())
                .volume(upstoxQuote.getVolume())
                .timestamp(upstoxQuote.getTimestamp())
                .build();
    }
}
```

---

### Phase 2: Clean API Module

#### Step 2.1: Move Service Implementations OUT of API

**From**: `market-data-api/src/main/java/com/am/marketdata/api/service/`
**To**: `market-data-service/src/main/java/com/am/marketdata/service/impl/`

**Files to move**:
- BrokerageCalculatorApiService.java → BrokerageCalculatorServiceImpl.java
- MarginCalculatorApiService.java → MarginCalculatorServiceImpl.java
- MarketAnalyticsService.java → MarketAnalyticsServiceImpl.java
- MarketDataPollingService.java → MarketDataPollingServiceImpl.java
- StockIndicesService.java → StockIndicesServiceImpl.java
- impl/MarketDataFetchServiceImpl.java (already named correctly, just move)

#### Step 2.2: Keep ONLY Interfaces in API

**Create interfaces in API module**:
`market-data-api/src/main/java/com/am/marketdata/api/service/`

```java
public interface BrokerageCalculatorService {
    BrokerageCalculationResponse calculate(BrokerageCalculationRequest request);
}

public interface MarketDataFetchService {
    Map<String, OHLCQuote> fetchQuotes(QuotesRequest request);
    Map<String, List<OHLCQuote>> fetchHistoricalData(HistoricalDataRequest request);
}
// ... etc
```

#### Step 2.3: Update Controllers
Controllers should inject interfaces, not implementations:

```java
@RestController
@RequestMapping("/api/v1/market-data")  // Note: versioned!
public class MarketDataController {
    
    private final MarketDataFetchService marketDataService;  // Interface
    
    public MarketDataController(MarketDataFetchService marketDataService) {
        this.marketDataService = marketDataService;
    }
    
    @PostMapping("/quotes")
    public ResponseEntity<Map<String, OHLCQuote>> getQuotes(@RequestBody QuotesRequest request) {
        return ResponseEntity.ok(marketDataService.fetchQuotes(request));
    }
}
```

---

### Phase 3: Introduce API Versioning

#### Step 3.1: Create Versioned Request/Response DTOs

**Current**: DTOs are in `market-data-common/src/main/java/com/marketdata/common/dto/`

**New Structure**:
```
market-data-common/
└── src/main/java/com/marketdata/common/dto/
    ├── v1/
    │   ├── HistoricalDataRequestV1.java
    │   ├── QuotesRequestV1.java
    │   └── QuotesResponseV1.java
    │
    └── v2/
        ├── HistoricalDataRequestV2.java  # Future
        └── ...
```

#### Step 3.2: Version Controllers

**Create separate controller packages**:
```
market-data-api/src/main/java/com/am/marketdata/api/controller/
├── v1/
│   ├── MarketDataControllerV1.java
│   ├── MarketAnalyticsControllerV1.java
│   └── ...
│
└── v2/  # Future
    └── ...
```

#### Step 3.3: Update Controller Mappings

```java
@RestController
@RequestMapping("/api/v1/market-data")
@Tag(name = "Market Data API v1")
public class MarketDataControllerV1 {
    // Uses v1 DTOs
}
```

---

### Phase 4: Update Service Layer to Use Provider

#### Step 4.1: Inject Provider Interface

`MarketDataFetchServiceImpl.java`:
```java
@Service
public class MarketDataFetchServiceImpl implements MarketDataFetchService {
    
    private final MarketDataProvider provider;  // Injected
    
    public MarketDataFetchServiceImpl(MarketDataProvider provider) {
        this.provider = provider;
    }
    
    @Override
    public Map<String, OHLCQuote> fetchQuotes(QuotesRequest request) {
        // Service orchestration logic (caching, validation, etc.)
        
        // Delegate to provider for actual data fetch
        Map<String, OHLCQuote> quotes = provider.getQuotes(request.getSymbols());
        
        // Post-processing
        return quotes;
    }
}
```

#### Step 4.2: Configure Provider Selection

`application.yml`:
```yaml
market.data.provider: upstox  # or zerodha
```

`ProviderConfig.java`:
```java
@Configuration
public class ProviderConfig {
    
    @Value("${market.data.provider}")
    private String providerType;
    
    @Bean
    public MarketDataProvider marketDataProvider(
            UpstoxMarketDataProvider upstoxProvider,
            ZerodhaMarketDataProvider zerodhaProvider) {
        
        return switch (providerType.toLowerCase()) {
            case "upstox" -> upstoxProvider;
            case "zerodha" -> zerodhaProvider;
            default -> throw new IllegalArgumentException("Unknown provider: " + providerType);
        };
    }
}
```

---

## Final Module Dependencies

```
market-data-common     → ZERO dependencies
market-data-provider   → depends on: common
market-data-api        → depends on: common
market-data-service    → depends on: api, common, provider (interface)
market-data-app        → depends on: service, provider (implementations)
```

---

## Migration Checklist

### ✅ Phase 0: Create SDK and Parser Infrastructure

#### SDK Module Setup
- [ ] Create `market-data-sdk` parent directory
- [ ] Create subdirectories:
  - [ ] `market-data-sdk-python/`
  - [ ] `market-data-sdk-flutter/`
  - [ ] `market-data-sdk-java/`
- [ ] Create `generate_sdks.ps1` script
- [ ] Create `generate_sdks.sh` script  
- [ ] Create `DESIGN.md` documentation
- [ ] Add `.gitignore` for generated files

#### Parser Module Setup
- [ ] Create `market-data-parser` module structure
- [ ] Create `pom.xml` with dependencies:
  - [ ] swagger-parser
  - [ ] openapi-generator
  - [ ] jackson
- [ ] Implement core classes:
  - [ ] `OpenApiParser.java` - Parse openapi.json
  - [ ] `SchemaValidator.java` - Validate spec completeness
  - [ ] `ModelExtractor.java` - Extract model definitions
- [ ] Implement generator interfaces:
  - [ ] `SdkGenerator.java` (base interface)
  - [ ] `PythonSdkGenerator.java`
  - [ ] `DartSdkGenerator.java`
  - [ ] `JavaSdkGenerator.java`
- [ ] Create model classes:
  - [ ] `ParsedSchema.java`
  - [ ] `ApiEndpoint.java`
  - [ ] `ModelDefinition.java`
- [ ] Add configuration:
  - [ ] `GeneratorConfig.java`
  - [ ] `generator-config.yml`
- [ ] Add unit tests for parser
- [ ] Add integration tests for SDK generation
- [ ] Compile and verify

#### OpenAPI Generation Test
- [ ] Create `OpenApiGeneratorTest.java` in service module
- [ ] Configure test to generate `openapi.json`
- [ ] Add validation for expected models
- [ ] Run test and verify spec generation
- [ ] Verify all models from common module are included

### ✅ Phase 1: Create Provider Module
- [ ] Create `market-data-provider` module
- [ ] Create `MarketDataProvider` interface
- [ ] Move Upstox code to `provider/upstox/`
- [ ] Implement `UpstoxMarketDataProvider` implementing interface
- [ ] Create `UpstoxToCommonMapper`
- [ ] Move Zerodha code (if exists)
- [ ] Add unit tests for providers
- [ ] Compile and verify

### ✅ Phase 2: Clean API Module
- [ ] Extract service interfaces from API
- [ ] Move all `*Service.java` implementations from API → Service
- [ ] Update API module to contain ONLY:
  - Controllers
  - Service interfaces
  - NO implementations
- [ ] Compile API module (should have NO service layer dependencies)

### ✅ Phase 3: API Versioning
- [ ] Create `dto/v1/` package in common
- [ ] Move current DTOs to v1
- [ ] Create `controller/v1/` in API
- [ ] Update all controller paths to `/api/v1/...`
- [ ] Update Swagger/OpenAPI config for versioning

### ✅ Phase 4: Update Service Layer
- [ ] Inject `MarketDataProvider` (interface) into services
- [ ] Remove all direct Upstox/Zerodha SDK calls
- [ ] Use provider interface instead
- [ ] Create `ProviderConfig` for runtime selection
- [ ] Add integration tests

### ✅ Phase 5: SDK Generation & Verification
- [ ] Run `mvn clean compile` on each module independently
- [ ] Verify dependency flow: API → Common, Service → API + Common + Provider
- [ ] Run all tests
- [ ] Generate OpenAPI spec: `mvn test -Dtest=OpenApiGeneratorTest`
- [ ] Verify `openapi.json` contains all expected models
- [ ] Run SDK generation: `./generate_sdks.ps1`
- [ ] Verify SDK generation:
  - [ ] Python SDK has all models (OHLCQuote, TimeFrame, etc.)
  - [ ] Dart SDK has all models
  - [ ] Java SDK has all models  
- [ ] Test SDK usage:
  - [ ] Python: Import and use models
  - [ ] Dart: Import and use models
  - [ ] Java: Import and use models
- [ ] Verify type safety in all SDKs
- [ ] Run SDK unit tests (if generated)
- [ ] Document SDK usage examples

---

## Benefits of This Refactoring

1. **Clean Separation**: API, Service, Provider are completely isolated
2. **Easy Provider Switching**: Change config, no code changes
3. **Testability**: Mock providers easily
4. **API Evolution**: v1, v2 can coexist
5. **SDK Quality**: Clean, versioned APIs generate better SDKs
6. **Compliance**: Follows all architectural rules

---

## Next Steps

1. Review this plan
2. Confirm approach
3. Start with Phase 1 (Provider module)
4. Iterate through phases
5. Verify at each step

Would you like me to start implementing Phase 1?
