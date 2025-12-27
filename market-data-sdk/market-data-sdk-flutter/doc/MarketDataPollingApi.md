# market_data_client.api.MarketDataPollingApi

## Load the API package
```dart
import 'package:market_data_client/api.dart';
```

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getConnectionStatus**](MarketDataPollingApi.md#getconnectionstatus) | **GET** /api/v1/polling/status | Get connection status
[**getUpdateSchema**](MarketDataPollingApi.md#getupdateschema) | **GET** /api/v1/polling/schema/update | Internal use for SDK generation
[**pollMarketData**](MarketDataPollingApi.md#pollmarketdata) | **GET** /api/v1/polling/data | Poll market data
[**subscribe**](MarketDataPollingApi.md#subscribe) | **POST** /api/v1/polling/subscribe | Subscribe to symbols
[**unsubscribe**](MarketDataPollingApi.md#unsubscribe) | **POST** /api/v1/polling/unsubscribe | Unsubscribe from symbols


# **getConnectionStatus**
> Map<String, Object> getConnectionStatus()

Get connection status

Returns streaming connection status

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketDataPollingApi();

try {
    final result = api_instance.getConnectionStatus();
    print(result);
} catch (e) {
    print('Exception when calling MarketDataPollingApi->getConnectionStatus: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Map<String, Object>**](Object.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getUpdateSchema**
> MarketDataUpdateV1 getUpdateSchema()

Internal use for SDK generation

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketDataPollingApi();

try {
    final result = api_instance.getUpdateSchema();
    print(result);
} catch (e) {
    print('Exception when calling MarketDataPollingApi->getUpdateSchema: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**MarketDataUpdateV1**](MarketDataUpdateV1.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **pollMarketData**
> Map<String, Object> pollMarketData(symbols, timeFrame, indexSymbol)

Poll market data

Poll live market data for given symbols

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketDataPollingApi();
final symbols = symbols_example; // String | 
final timeFrame = timeFrame_example; // String | 
final indexSymbol = true; // bool | 

try {
    final result = api_instance.pollMarketData(symbols, timeFrame, indexSymbol);
    print(result);
} catch (e) {
    print('Exception when calling MarketDataPollingApi->pollMarketData: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **symbols** | **String**|  | 
 **timeFrame** | **String**|  | [optional] [default to '1m']
 **indexSymbol** | **bool**|  | [optional] [default to false]

### Return type

[**Map<String, Object>**](Object.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **subscribe**
> Map<String, Object> subscribe(requestBody)

Subscribe to symbols

Subscribe to symbols for real-time updates

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketDataPollingApi();
final requestBody = [List<String>()]; // List<String> | 

try {
    final result = api_instance.subscribe(requestBody);
    print(result);
} catch (e) {
    print('Exception when calling MarketDataPollingApi->subscribe: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **requestBody** | [**List<String>**](String.md)|  | 

### Return type

[**Map<String, Object>**](Object.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **unsubscribe**
> Map<String, Object> unsubscribe(requestBody)

Unsubscribe from symbols

Unsubscribe from symbols

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketDataPollingApi();
final requestBody = [List<String>()]; // List<String> | 

try {
    final result = api_instance.unsubscribe(requestBody);
    print(result);
} catch (e) {
    print('Exception when calling MarketDataPollingApi->unsubscribe: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **requestBody** | [**List<String>**](String.md)|  | 

### Return type

[**Map<String, Object>**](Object.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

