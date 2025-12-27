# market_data_client.api.MarginCalculatorApi

## Load the API package
```dart
import 'package:market_data_client/api.dart';
```

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**calculateMargin**](MarginCalculatorApi.md#calculatemargin) | **POST** /api/v1/margin/calculate | Calculate margin requirements


# **calculateMargin**
> MarginCalculationResponse calculateMargin(marginCalculationRequest)

Calculate margin requirements

Calculates SPAN, exposure, and total margin requirements for various positions

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = MarginCalculatorApi();
final marginCalculationRequest = MarginCalculationRequest(); // MarginCalculationRequest | 

try {
    final result = api_instance.calculateMargin(marginCalculationRequest);
    print(result);
} catch (e) {
    print('Exception when calling MarginCalculatorApi->calculateMargin: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **marginCalculationRequest** | [**MarginCalculationRequest**](MarginCalculationRequest.md)|  | 

### Return type

[**MarginCalculationResponse**](MarginCalculationResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

