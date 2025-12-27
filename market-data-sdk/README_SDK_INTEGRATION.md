# Market Data SDK - Integration Guide

## ✅ SDK Generation & Verification Status

### All SDKs Successfully Generated & Verified!

**Verification Date:** 2025-12-28  
**Status:** ✅ ALL PASS

| SDK | Status | API Clients | Build Status |
|-----|--------|-------------|--------------|
| **Java** | ✅ PASS | 8/8 | ✅ BUILD SUCCESS |
| **Python** | ✅ PASS | 8/8 | ⏳ Not Built (Runtime) |
| **Dart/Flutter** | ✅ PASS | 8/8 | ⏳ Pending Integration |

### Generated API Clients (8 Total)

All SDKs include the following API clients:

1. **BrokerageCalculatorApi** - Brokerage calculation endpoints
2. **MarginCalculatorApi** - Margin calculation endpoints
3. **MarketAnalyticsApi** - Market analytics and insights
4. **MarketDataApi** - Core market data operations
5. **MarketDataPollingApi** - Real-time polling endpoints
6. **MarketIndicesApi** - Market indices data
7. **SecurityMetadataApi** - Security/instrument metadata
8. **StockIndicesApi** - Stock indices data

---

## 📦 SDK Locations

```
market-data-sdk/
├── market-data-sdk-java/       # Java SDK (Maven)
├── market-data-sdk-python/     # Python SDK (pip)
├── market-data-sdk-flutter/    # Dart/Flutter SDK (pub)
├── generate_sdks.ps1           # SDK generation script
└── verify_sdk_generation.py    # Verification script
```

---

## 🚀 Usage Instructions

### 1. Java SDK

**Installation (Maven):**
```xml
<dependency>
    <groupId>com.am</groupId>
    <artifactId>market-data-sdk-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Usage Example:**
```java
import com.am.marketdata.api.MarketDataApi;
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.model.*;

// Initialize client
ApiClient client = new ApiClient();
client.setBasePath("http://localhost:8080");

// Create API instance
MarketDataApi api = new MarketDataApi(client);

// Fetch market data
OHLCQuoteV1 quote = api.getOHLC("RELIANCE", "1D", false);
```

### 2. Python SDK

**Installation:**
```bash
cd market-data-sdk/market-data-sdk-python
pip install -e .
```

**Usage Example:**
```python
from market_data_client import ApiClient, Configuration
from market_data_client.api import MarketDataApi

# Configure client
config = Configuration()
config.host = "http://localhost:8080"
client = ApiClient(configuration=config)

# Create API instance
api = MarketDataApi(client)

# Fetch market data
quote = api.get_ohlc(symbol="RELIANCE", time_frame="1D", force_refresh=False)
```

### 3. Dart/Flutter SDK

**Installation (pubspec.yaml):**
```yaml
dependencies:
  market_data_client:
    path: ../market-data-sdk/market-data-sdk-flutter
```

**Usage Example:**
```dart
import 'package:market_data_client/api.dart';

// Initialize client
final api = MarketDataApi();

// Fetch market data
final quote = await api.getOHLC(
  symbol: 'RELIANCE',
  timeFrame: '1D',
  forceRefresh: false,
);
```

---

## 🔄 Regenerating SDKs

### Automatic (Recommended)

Run the generation script:
```powershell
cd market-data-sdk
.\generate_sdks.ps1
```

This will:
1. Generate OpenAPI spec from `market-data-api` tests
2. Generate Java SDK
3. Generate Python SDK
4. Generate Dart/Flutter SDK

### Verification

Run the verification script to ensure all SDKs are correct:
```bash
cd market-data-sdk
python verify_sdk_generation.py
```

---

## 🛡️ Git Configuration

### ⚠️ IMPORTANT: Generated Files Are NOT Committed

All SDK generated files are excluded from Git via `.gitignore`:

```gitignore
# SDK Generated Files - Auto-generated from OpenAPI spec
market-data-sdk/market-data-sdk-java/src/
market-data-sdk/market-data-sdk-python/
market-data-sdk/market-data-sdk-flutter/
```

**What IS committed:**
- ✅ `generate_sdks.ps1` - Generation script
- ✅ `verify_sdk_generation.py` - Verification script
- ✅ `docs/` - Documentation
- ✅ `.gitignore` - Git configuration

**What is NOT committed:**
- ❌ Generated SDK source code
- ❌ Generated models
- ❌ Generated API clients
- ❌ Build artifacts

### Why?

1. **Source of Truth:** The OpenAPI spec in `market-data-api` is the single source of truth
2. **Always Fresh:** SDKs are regenerated from the latest API changes
3. **No Conflicts:** Avoids merge conflicts in generated code
4. **Smaller Repo:** Keeps repository size manageable

---

## 🔧 Build Integration

### Maven Build (Java SDK)

The Java SDK is built as part of the main Maven reactor:

```bash
# Build entire project (includes SDK)
mvn clean install

# Build only SDK
mvn clean install -pl market-data-sdk/market-data-sdk-java
```

### CI/CD Pipeline

**Recommended workflow:**

1. **On API Changes:**
   ```bash
   # Step 1: Build API module (generates OpenAPI spec)
   mvn clean install -pl market-data-api
   
   # Step 2: Regenerate SDKs
   cd market-data-sdk
   .\generate_sdks.ps1
   
   # Step 3: Verify SDKs
   python verify_sdk_generation.py
   
   # Step 4: Build Java SDK
   mvn clean install -pl market-data-sdk-java
   ```

2. **Publish SDKs** (if needed):
   - Java: Deploy to Maven repository
   - Python: Publish to PyPI
   - Dart: Publish to pub.dev

---

## 📋 Troubleshooting

### SDK Generation Fails

**Problem:** `openapi.json` not found  
**Solution:** Run tests in `market-data-api` first:
```bash
cd market-data-api
mvn test -Dtest=OpenApiGeneratorTest
```

### Java SDK Build Fails

**Problem:** Compilation errors in generated code  
**Solution:** 
1. Check OpenAPI spec validity
2. Regenerate SDKs
3. Ensure Java 17+ is installed

### Flutter SDK Issues

**Problem:** Package not found  
**Solution:**
```bash
cd market_data_web
flutter pub get
```

---

## 📚 Additional Resources

- **OpenAPI Generator:** https://openapi-generator.tech/
- **API Documentation:** See `market-data-api/target/openapi.json`
- **SDK Docs:** Each SDK includes generated documentation in `docs/` folder

---

## ✅ Verification Checklist

Before committing changes:

- [ ] Run `mvn clean install` - All modules build successfully
- [ ] Run `.\generate_sdks.ps1` - SDKs regenerate without errors
- [ ] Run `python verify_sdk_generation.py` - All 8 API clients present
- [ ] Run `mvn clean install -pl market-data-sdk-java` - Java SDK builds
- [ ] Verify `.gitignore` excludes generated files
- [ ] Test SDK integration in client applications

---

**Last Updated:** 2025-12-28  
**Maintained By:** AM Portfolio Team
