# SDK Usage Guide

This guide provides quick-start examples for using the auto-generated Market Data SDKs.

## Java SDK

### Installation

Add to your `pom.xml`:
```xml
<dependency>
    <groupId>com.am.marketdata</groupId>
    <artifactId>market-data-sdk-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Usage Example

```java
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.api.MarketDataApi;
import com.am.marketdata.client.model.OHLCQuote;

public class MarketDataExample {
    public static void main(String[] args) {
        // Configure API client
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");
        
        // Create API instance
        MarketDataApi apiInstance = new MarketDataApi(defaultClient);
        
        try {
            // Fetch market data
            Map<String, OHLCQuote> result = apiInstance.fetchMarketData(
                Arrays.asList("NIFTY 50", "NIFTY BANK"),
                "1D",
                true
            );
            
            System.out.println("Market Data: " + result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataApi#fetchMarketData");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

## Python SDK

### Installation

```bash
cd market-data-sdk-python
pip install -e .
```

Or via pip (when published):
```bash
pip install market-data-client
```

### Usage Example

```python
from market_data_client import ApiClient, Configuration
from market_data_client.api.market_data_api import MarketDataApi
from market_data_client.exceptions import ApiException

# Configure API client
configuration = Configuration()
configuration.host = "http://localhost:8080"

# Create API instance
with ApiClient(configuration) as api_client:
    api_instance = MarketDataApi(api_client)
    
    try:
        # Fetch market data
        symbols = ["NIFTY 50", "NIFTY BANK"]
        time_frame = "1D"
        fetch_index_stocks = True
        
        result = api_instance.fetch_market_data(
            symbols=symbols,
            time_frame=time_frame,
            fetch_index_stocks=fetch_index_stocks
        )
        
        print(f"Market Data: {result}")
    except ApiException as e:
        print(f"Exception when calling MarketDataApi->fetch_market_data: {e}")
```

### Async Usage (Python)

```python
import asyncio
from market_data_client import ApiClient, Configuration
from market_data_client.api.market_data_api import MarketDataApi

async def fetch_data():
    configuration = Configuration()
    configuration.host = "http://localhost:8080"
    
    async with ApiClient(configuration) as api_client:
        api_instance = MarketDataApi(api_client)
        
        result = await api_instance.fetch_market_data(
            symbols=["NIFTY 50"],
            time_frame="1D"
        )
        
        return result

# Run async function
data = asyncio.run(fetch_data())
print(data)
```

## Dart/Flutter SDK

### Installation

Add to your `pubspec.yaml`:
```yaml
dependencies:
  market_data_client:
    path: ../market-data-sdk-flutter
```

Or (when published):
```yaml
dependencies:
  market_data_client: ^1.0.0
```

### Usage Example

```dart
import 'package:market_data_client/api.dart';

void main() async {
  final apiClient = ApiClient(basePath: 'http://localhost:8080');
  final marketDataApi = MarketDataApi(apiClient);
  
  try {
    // Fetch market data
    final result = await marketDataApi.fetchMarketData(
      symbols: ['NIFTY 50', 'NIFTY BANK'],
      timeFrame: '1D',
      fetchIndexStocks: true,
    );
    
    print('Market Data: $result');
  } catch (e) {
    print('Exception when calling MarketDataApi->fetchMarketData: $e');
  }
}
```

### Flutter Widget Example

```dart
import 'package:flutter/material.dart';
import 'package:market_data_client/api.dart';

class MarketDataWidget extends StatefulWidget {
  @override
  _MarketDataWidgetState createState() => _MarketDataWidgetState();
}

class _MarketDataWidgetState extends State<MarketDataWidget> {
  final MarketDataApi _api = MarketDataApi(
    ApiClient(basePath: 'http://localhost:8080')
  );
  
  Map<String, OHLCQuote>? _marketData;
  bool _isLoading = false;
  String? _error;
  
  @override
  void initState() {
    super.initState();
    _fetchData();
  }
  
  Future<void> _fetchData() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    
    try {
      final result = await _api.fetchMarketData(
        symbols: ['NIFTY 50', 'NIFTY BANK'],
        timeFrame: '1D',
      );
      
      setState(() {
        _marketData = result;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }
  
  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return Center(child: CircularProgressIndicator());
    }
    
    if (_error != null) {
      return Center(child: Text('Error: $_error'));
    }
    
    if (_marketData == null) {
      return Center(child: Text('No data'));
    }
    
    return ListView.builder(
      itemCount: _marketData!.length,
      itemBuilder: (context, index) {
        final symbol = _marketData!.keys.elementAt(index);
        final quote = _marketData![symbol]!;
        
        return ListTile(
          title: Text(symbol),
          subtitle: Text('LTP: ${quote.lastPrice}'),
          trailing: Text(
            '${quote.change >= 0 ? '+' : ''}${quote.change.toStringAsFixed(2)}',
            style: TextStyle(
              color: quote.change >= 0 ? Colors.green : Colors.red,
            ),
          ),
        );
      },
    );
  }
}
```

## Common Configuration

### Authentication

If your API requires JWT authentication:

**Java**:
```java
defaultClient.setRequestInterceptor(request -> {
    request.header("Authorization", "Bearer " + jwtToken);
});
```

**Python**:
```python
configuration.access_token = "your_jwt_token"
```

**Dart**:
```dart
apiClient.addDefaultHeader('Authorization', 'Bearer $jwtToken');
```

### Custom Base URL

**Java**:
```java
defaultClient.setBasePath("https://api.example.com");
```

**Python**:
```python
configuration.host = "https://api.example.com"
```

**Dart**:
```dart
final apiClient = ApiClient(basePath: 'https://api.example.com');
```

### Timeout Configuration

**Java**:
```java
defaultClient.setConnectTimeout(Duration.ofSeconds(30));
defaultClient.setReadTimeout(Duration.ofSeconds(30));
```

**Python**:
```python
configuration.timeout = 30  # seconds
```

**Dart**:
```dart
apiClient.client.timeout = Duration(seconds: 30);
```

## Available APIs

All SDKs include the following API clients:

1. **MarketDataApi** - Core market data operations
2. **MarketIndicesApi** - Index-related operations
3. **StockIndicesApi** - Stock indices data
4. **MarketAnalyticsApi** - Analytics and insights
5. **MarketDataPollingApi** - Polling and streaming
6. **BrokerageCalculatorApi** - Brokerage calculations
7. **MarginCalculatorApi** - Margin calculations
8. **SecurityMetadataApi** - Security metadata operations

## Error Handling

### Java
```java
try {
    // API call
} catch (ApiException e) {
    System.err.println("Status code: " + e.getCode());
    System.err.println("Response body: " + e.getResponseBody());
    System.err.println("Response headers: " + e.getResponseHeaders());
}
```

### Python
```python
from market_data_client.exceptions import ApiException

try:
    # API call
except ApiException as e:
    print(f"Status: {e.status}")
    print(f"Reason: {e.reason}")
    print(f"Body: {e.body}")
```

### Dart
```dart
try {
  // API call
} on ApiException catch (e) {
  print('Status code: ${e.code}');
  print('Message: ${e.message}');
}
```

## Testing

### Java
```java
@Test
public void testFetchMarketData() throws ApiException {
    MarketDataApi api = new MarketDataApi();
    Map<String, OHLCQuote> result = api.fetchMarketData(
        Arrays.asList("NIFTY 50"),
        "1D",
        false
    );
    assertNotNull(result);
    assertTrue(result.containsKey("NIFTY 50"));
}
```

### Python
```python
import unittest
from market_data_client.api.market_data_api import MarketDataApi

class TestMarketDataApi(unittest.TestCase):
    def test_fetch_market_data(self):
        api = MarketDataApi()
        result = api.fetch_market_data(
            symbols=["NIFTY 50"],
            time_frame="1D"
        )
        self.assertIsNotNone(result)
        self.assertIn("NIFTY 50", result)
```

### Dart
```dart
import 'package:test/test.dart';
import 'package:market_data_client/api.dart';

void main() {
  test('Fetch market data', () async {
    final api = MarketDataApi();
    final result = await api.fetchMarketData(
      symbols: ['NIFTY 50'],
      timeFrame: '1D',
    );
    
    expect(result, isNotNull);
    expect(result.containsKey('NIFTY 50'), isTrue);
  });
}
```

## Regenerating SDKs

When the API changes, regenerate all SDKs:

```powershell
# From project root
.\market-data-sdk\generate_sdks.ps1
```

This will:
1. Run tests to generate the latest `openapi.json`
2. Generate fresh Python, Dart, and Java SDKs
3. Overwrite existing SDK code

## Publishing SDKs

### Java (Maven Central)
```bash
cd market-data-sdk-java
mvn clean deploy
```

### Python (PyPI)
```bash
cd market-data-sdk-python
python setup.py sdist bdist_wheel
twine upload dist/*
```

### Dart (pub.dev)
```bash
cd market-data-sdk-flutter
dart pub publish
```

## Support

For issues or questions:
- API Documentation: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs
- GitHub Issues: [Your Repository URL]
