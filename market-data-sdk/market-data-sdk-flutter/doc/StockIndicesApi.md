# market_data_client.api.StockIndicesApi

## Load the API package
```dart
import 'package:market_data_client/api.dart';
```

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getAvailableIndices**](StockIndicesApi.md#getavailableindices) | **GET** /api/v1/stock-indices/available | Get available indices
[**getStockIndices**](StockIndicesApi.md#getstockindices) | **GET** /api/v1/stock-indices/{indexSymbol} | Get stock indices data
[**getStockIndicesBatch**](StockIndicesApi.md#getstockindicesbatch) | **POST** /api/v1/stock-indices/batch | Get multiple stock indices


# **getAvailableIndices**
> Map<String, List<String>> getAvailableIndices()

Get available indices

Returns list of available broad market and sector indices

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = StockIndicesApi();

try {
    final result = api_instance.getAvailableIndices();
    print(result);
} catch (e) {
    print('Exception when calling StockIndicesApi->getAvailableIndices: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Map<String, List<String>>**](List.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getStockIndices**
> NSEStockIndicesDataV1 getStockIndices(indexSymbol, forceRefresh)

Get stock indices data

Returns constituent stocks for a specific index

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = StockIndicesApi();
final indexSymbol = indexSymbol_example; // String | 
final forceRefresh = true; // bool | 

try {
    final result = api_instance.getStockIndices(indexSymbol, forceRefresh);
    print(result);
} catch (e) {
    print('Exception when calling StockIndicesApi->getStockIndices: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **indexSymbol** | **String**|  | 
 **forceRefresh** | **bool**|  | [optional] [default to false]

### Return type

[**NSEStockIndicesDataV1**](NSEStockIndicesDataV1.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getStockIndicesBatch**
> List<NSEStockIndicesDataV1> getStockIndicesBatch(requestBody, forceRefresh)

Get multiple stock indices

Returns constituent stocks for multiple indices

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = StockIndicesApi();
final requestBody = [List<String>()]; // List<String> | 
final forceRefresh = true; // bool | 

try {
    final result = api_instance.getStockIndicesBatch(requestBody, forceRefresh);
    print(result);
} catch (e) {
    print('Exception when calling StockIndicesApi->getStockIndicesBatch: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **requestBody** | [**List<String>**](String.md)|  | 
 **forceRefresh** | **bool**|  | [optional] [default to false]

### Return type

[**List<NSEStockIndicesDataV1>**](NSEStockIndicesDataV1.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

