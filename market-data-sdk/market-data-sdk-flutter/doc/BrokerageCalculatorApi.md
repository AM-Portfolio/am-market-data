# market_data_client.api.BrokerageCalculatorApi

## Load the API package
```dart
import 'package:market_data_client/api.dart';
```

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**calculateBrokerage**](BrokerageCalculatorApi.md#calculatebrokerage) | **POST** /api/v1/brokerage/calculate | Calculate brokerage and charges


# **calculateBrokerage**
> BrokerageCalculationResponse calculateBrokerage(brokerageCalculationRequest)

Calculate brokerage and charges

Calculates total brokerage, taxes, and other charges based on trade details

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = BrokerageCalculatorApi();
final brokerageCalculationRequest = BrokerageCalculationRequest(); // BrokerageCalculationRequest | 

try {
    final result = api_instance.calculateBrokerage(brokerageCalculationRequest);
    print(result);
} catch (e) {
    print('Exception when calling BrokerageCalculatorApi->calculateBrokerage: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **brokerageCalculationRequest** | [**BrokerageCalculationRequest**](BrokerageCalculationRequest.md)|  | 

### Return type

[**BrokerageCalculationResponse**](BrokerageCalculationResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

