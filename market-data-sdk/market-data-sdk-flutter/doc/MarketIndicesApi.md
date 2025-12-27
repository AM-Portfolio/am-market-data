# market_data_client.api.MarketIndicesApi

## Load the API package
```dart
import 'package:market_data_client/api.dart';
```

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getAllIndices**](MarketIndicesApi.md#getallindices) | **GET** /api/v1/indices/all | Get all market indices
[**getIndex**](MarketIndicesApi.md#getindex) | **GET** /api/v1/indices/{symbol} | Get index by symbol


# **getAllIndices**
> List<NSEIndexV1> getAllIndices()

Get all market indices

Returns all available market indices

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketIndicesApi();

try {
    final result = api_instance.getAllIndices();
    print(result);
} catch (e) {
    print('Exception when calling MarketIndicesApi->getAllIndices: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List<NSEIndexV1>**](NSEIndexV1.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getIndex**
> NSEIndexV1 getIndex(symbol)

Get index by symbol

Returns a specific index by its symbol

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketIndicesApi();
final symbol = symbol_example; // String | 

try {
    final result = api_instance.getIndex(symbol);
    print(result);
} catch (e) {
    print('Exception when calling MarketIndicesApi->getIndex: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **symbol** | **String**|  | 

### Return type

[**NSEIndexV1**](NSEIndexV1.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

