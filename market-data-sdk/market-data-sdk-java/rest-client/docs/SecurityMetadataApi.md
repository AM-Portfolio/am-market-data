# SecurityMetadataApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**findBySymbols**](SecurityMetadataApi.md#findBySymbols) | **GET** /api/v1/security/find | Find securities by symbols |
| [**findBySymbolsWithHttpInfo**](SecurityMetadataApi.md#findBySymbolsWithHttpInfo) | **GET** /api/v1/security/find | Find securities by symbols |
| [**getAll**](SecurityMetadataApi.md#getAll) | **GET** /api/v1/security/all | Get all securities |
| [**getAllWithHttpInfo**](SecurityMetadataApi.md#getAllWithHttpInfo) | **GET** /api/v1/security/all | Get all securities |
| [**getSectors**](SecurityMetadataApi.md#getSectors) | **GET** /api/v1/security/sectors | Get sectors for symbols |
| [**getSectorsWithHttpInfo**](SecurityMetadataApi.md#getSectorsWithHttpInfo) | **GET** /api/v1/security/sectors | Get sectors for symbols |
| [**search**](SecurityMetadataApi.md#search) | **POST** /api/v1/security/search | Search securities |
| [**searchWithHttpInfo**](SecurityMetadataApi.md#searchWithHttpInfo) | **POST** /api/v1/security/search | Search securities |



## findBySymbols

> List<SecurityDTOV1> findBySymbols(symbols)

Find securities by symbols

Returns metadata for specific symbols

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.SecurityMetadataApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        SecurityMetadataApi apiInstance = new SecurityMetadataApi(defaultClient);
        String symbols = "symbols_example"; // String | 
        try {
            List<SecurityDTOV1> result = apiInstance.findBySymbols(symbols);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SecurityMetadataApi#findBySymbols");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **symbols** | **String**|  | |

### Return type

[**List&lt;SecurityDTOV1&gt;**](SecurityDTOV1.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## findBySymbolsWithHttpInfo

> ApiResponse<List<SecurityDTOV1>> findBySymbols findBySymbolsWithHttpInfo(symbols)

Find securities by symbols

Returns metadata for specific symbols

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.SecurityMetadataApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        SecurityMetadataApi apiInstance = new SecurityMetadataApi(defaultClient);
        String symbols = "symbols_example"; // String | 
        try {
            ApiResponse<List<SecurityDTOV1>> response = apiInstance.findBySymbolsWithHttpInfo(symbols);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling SecurityMetadataApi#findBySymbols");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **symbols** | **String**|  | |

### Return type

ApiResponse<[**List&lt;SecurityDTOV1&gt;**](SecurityDTOV1.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getAll

> List<SecurityDTOV1> getAll()

Get all securities

Returns all available securities in the database

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.SecurityMetadataApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        SecurityMetadataApi apiInstance = new SecurityMetadataApi(defaultClient);
        try {
            List<SecurityDTOV1> result = apiInstance.getAll();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SecurityMetadataApi#getAll");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**List&lt;SecurityDTOV1&gt;**](SecurityDTOV1.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getAllWithHttpInfo

> ApiResponse<List<SecurityDTOV1>> getAll getAllWithHttpInfo()

Get all securities

Returns all available securities in the database

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.SecurityMetadataApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        SecurityMetadataApi apiInstance = new SecurityMetadataApi(defaultClient);
        try {
            ApiResponse<List<SecurityDTOV1>> response = apiInstance.getAllWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling SecurityMetadataApi#getAll");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

ApiResponse<[**List&lt;SecurityDTOV1&gt;**](SecurityDTOV1.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getSectors

> Map<String, String> getSectors(symbols)

Get sectors for symbols

Returns a mapping of symbol to sector

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.SecurityMetadataApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        SecurityMetadataApi apiInstance = new SecurityMetadataApi(defaultClient);
        String symbols = "symbols_example"; // String | 
        try {
            Map<String, String> result = apiInstance.getSectors(symbols);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SecurityMetadataApi#getSectors");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **symbols** | **String**|  | |

### Return type

**Map&lt;String, String&gt;**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getSectorsWithHttpInfo

> ApiResponse<Map<String, String>> getSectors getSectorsWithHttpInfo(symbols)

Get sectors for symbols

Returns a mapping of symbol to sector

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.SecurityMetadataApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        SecurityMetadataApi apiInstance = new SecurityMetadataApi(defaultClient);
        String symbols = "symbols_example"; // String | 
        try {
            ApiResponse<Map<String, String>> response = apiInstance.getSectorsWithHttpInfo(symbols);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling SecurityMetadataApi#getSectors");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **symbols** | **String**|  | |

### Return type

ApiResponse<**Map&lt;String, String&gt;**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## search

> List<SecurityDTOV1> search(securitySearchRequestV1)

Search securities

Search for securities using various filters

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.SecurityMetadataApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        SecurityMetadataApi apiInstance = new SecurityMetadataApi(defaultClient);
        SecuritySearchRequestV1 securitySearchRequestV1 = new SecuritySearchRequestV1(); // SecuritySearchRequestV1 | 
        try {
            List<SecurityDTOV1> result = apiInstance.search(securitySearchRequestV1);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SecurityMetadataApi#search");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **securitySearchRequestV1** | [**SecuritySearchRequestV1**](SecuritySearchRequestV1.md)|  | |

### Return type

[**List&lt;SecurityDTOV1&gt;**](SecurityDTOV1.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## searchWithHttpInfo

> ApiResponse<List<SecurityDTOV1>> search searchWithHttpInfo(securitySearchRequestV1)

Search securities

Search for securities using various filters

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.SecurityMetadataApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        SecurityMetadataApi apiInstance = new SecurityMetadataApi(defaultClient);
        SecuritySearchRequestV1 securitySearchRequestV1 = new SecuritySearchRequestV1(); // SecuritySearchRequestV1 | 
        try {
            ApiResponse<List<SecurityDTOV1>> response = apiInstance.searchWithHttpInfo(securitySearchRequestV1);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling SecurityMetadataApi#search");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **securitySearchRequestV1** | [**SecuritySearchRequestV1**](SecuritySearchRequestV1.md)|  | |

### Return type

ApiResponse<[**List&lt;SecurityDTOV1&gt;**](SecurityDTOV1.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

