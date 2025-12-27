# market_data_client.api.MarketAnalyticsApi

## Load the API package
```dart
import 'package:market_data_client/api.dart';
```

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getMarketBreadth**](MarketAnalyticsApi.md#getmarketbreadth) | **GET** /api/v1/analytics/breadth | Get market breadth
[**getMarketSummary**](MarketAnalyticsApi.md#getmarketsummary) | **GET** /api/v1/analytics/summary | Get market summary
[**getSectorPerformance**](MarketAnalyticsApi.md#getsectorperformance) | **GET** /api/v1/analytics/sectors | Get sector performance
[**getTopMovers**](MarketAnalyticsApi.md#gettopmovers) | **GET** /api/v1/analytics/movers | Get top gainers and losers


# **getMarketBreadth**
> Map<String, Object> getMarketBreadth()

Get market breadth

Returns market breadth data (advances, declines, unchanged)

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketAnalyticsApi();

try {
    final result = api_instance.getMarketBreadth();
    print(result);
} catch (e) {
    print('Exception when calling MarketAnalyticsApi->getMarketBreadth: $e\n');
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

# **getMarketSummary**
> Map<String, Object> getMarketSummary()

Get market summary

Returns overall market summary with key metrics

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketAnalyticsApi();

try {
    final result = api_instance.getMarketSummary();
    print(result);
} catch (e) {
    print('Exception when calling MarketAnalyticsApi->getMarketSummary: $e\n');
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

# **getSectorPerformance**
> Map<String, Object> getSectorPerformance()

Get sector performance

Returns sector-wise performance data

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketAnalyticsApi();

try {
    final result = api_instance.getSectorPerformance();
    print(result);
} catch (e) {
    print('Exception when calling MarketAnalyticsApi->getSectorPerformance: $e\n');
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

# **getTopMovers**
> Map<String, List<Object>> getTopMovers(limit)

Get top gainers and losers

Returns top gaining and losing stocks

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarketAnalyticsApi();
final limit = 56; // int | 

try {
    final result = api_instance.getTopMovers(limit);
    print(result);
} catch (e) {
    print('Exception when calling MarketAnalyticsApi->getTopMovers: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **limit** | **int**|  | [optional] [default to 10]

### Return type

[**Map<String, List<Object>>**](List.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

