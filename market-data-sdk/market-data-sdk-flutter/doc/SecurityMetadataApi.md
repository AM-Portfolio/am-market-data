# market_data_client.api.SecurityMetadataApi

## Load the API package
```dart
import 'package:market_data_client/api.dart';
```

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**findBySymbols**](SecurityMetadataApi.md#findbysymbols) | **GET** /api/v1/security/find | Find securities by symbols
[**getAll**](SecurityMetadataApi.md#getall) | **GET** /api/v1/security/all | Get all securities
[**getSectors**](SecurityMetadataApi.md#getsectors) | **GET** /api/v1/security/sectors | Get sectors for symbols
[**search**](SecurityMetadataApi.md#search) | **POST** /api/v1/security/search | Search securities


# **findBySymbols**
> List<SecurityDTOV1> findBySymbols(symbols)

Find securities by symbols

Returns metadata for specific symbols

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = SecurityMetadataApi();
final symbols = symbols_example; // String | 

try {
    final result = api_instance.findBySymbols(symbols);
    print(result);
} catch (e) {
    print('Exception when calling SecurityMetadataApi->findBySymbols: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **symbols** | **String**|  | 

### Return type

[**List<SecurityDTOV1>**](SecurityDTOV1.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAll**
> List<SecurityDTOV1> getAll()

Get all securities

Returns all available securities in the database

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = SecurityMetadataApi();

try {
    final result = api_instance.getAll();
    print(result);
} catch (e) {
    print('Exception when calling SecurityMetadataApi->getAll: $e\n');
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List<SecurityDTOV1>**](SecurityDTOV1.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getSectors**
> Map<String, String> getSectors(symbols)

Get sectors for symbols

Returns a mapping of symbol to sector

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = SecurityMetadataApi();
final symbols = symbols_example; // String | 

try {
    final result = api_instance.getSectors(symbols);
    print(result);
} catch (e) {
    print('Exception when calling SecurityMetadataApi->getSectors: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **symbols** | **String**|  | 

### Return type

**Map<String, String>**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **search**
> List<SecurityDTOV1> search(securitySearchRequestV1)

Search securities

Search for securities using various filters

### Example
```dart
import 'package:market_data_client/api.dart';

final api_instance = SecurityMetadataApi();
final securitySearchRequestV1 = SecuritySearchRequestV1(); // SecuritySearchRequestV1 | 

try {
    final result = api_instance.search(securitySearchRequestV1);
    print(result);
} catch (e) {
    print('Exception when calling SecurityMetadataApi->search: $e\n');
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **securitySearchRequestV1** | [**SecuritySearchRequestV1**](SecuritySearchRequestV1.md)|  | 

### Return type

[**List<SecurityDTOV1>**](SecurityDTOV1.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

