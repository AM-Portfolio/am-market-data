# MarketIndicesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getAllIndices**](MarketIndicesApi.md#getAllIndices) | **GET** /api/v1/indices/all | Get all market indices |
| [**getAllIndicesWithHttpInfo**](MarketIndicesApi.md#getAllIndicesWithHttpInfo) | **GET** /api/v1/indices/all | Get all market indices |
| [**getIndex**](MarketIndicesApi.md#getIndex) | **GET** /api/v1/indices/{symbol} | Get index by symbol |
| [**getIndexWithHttpInfo**](MarketIndicesApi.md#getIndexWithHttpInfo) | **GET** /api/v1/indices/{symbol} | Get index by symbol |



## getAllIndices

> List<NSEIndexV1> getAllIndices()

Get all market indices

Returns all available market indices

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketIndicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketIndicesApi apiInstance = new MarketIndicesApi(defaultClient);
        try {
            List<NSEIndexV1> result = apiInstance.getAllIndices();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketIndicesApi#getAllIndices");
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

[**List&lt;NSEIndexV1&gt;**](NSEIndexV1.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getAllIndicesWithHttpInfo

> ApiResponse<List<NSEIndexV1>> getAllIndices getAllIndicesWithHttpInfo()

Get all market indices

Returns all available market indices

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketIndicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketIndicesApi apiInstance = new MarketIndicesApi(defaultClient);
        try {
            ApiResponse<List<NSEIndexV1>> response = apiInstance.getAllIndicesWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketIndicesApi#getAllIndices");
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

ApiResponse<[**List&lt;NSEIndexV1&gt;**](NSEIndexV1.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getIndex

> NSEIndexV1 getIndex(symbol)

Get index by symbol

Returns a specific index by its symbol

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketIndicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketIndicesApi apiInstance = new MarketIndicesApi(defaultClient);
        String symbol = "symbol_example"; // String | 
        try {
            NSEIndexV1 result = apiInstance.getIndex(symbol);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketIndicesApi#getIndex");
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
| **symbol** | **String**|  | |

### Return type

[**NSEIndexV1**](NSEIndexV1.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getIndexWithHttpInfo

> ApiResponse<NSEIndexV1> getIndex getIndexWithHttpInfo(symbol)

Get index by symbol

Returns a specific index by its symbol

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketIndicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketIndicesApi apiInstance = new MarketIndicesApi(defaultClient);
        String symbol = "symbol_example"; // String | 
        try {
            ApiResponse<NSEIndexV1> response = apiInstance.getIndexWithHttpInfo(symbol);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketIndicesApi#getIndex");
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
| **symbol** | **String**|  | |

### Return type

ApiResponse<[**NSEIndexV1**](NSEIndexV1.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

