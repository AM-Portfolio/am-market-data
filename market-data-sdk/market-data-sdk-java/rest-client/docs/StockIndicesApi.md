# StockIndicesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getAvailableIndices**](StockIndicesApi.md#getAvailableIndices) | **GET** /api/v1/stock-indices/available | Get available indices |
| [**getAvailableIndicesWithHttpInfo**](StockIndicesApi.md#getAvailableIndicesWithHttpInfo) | **GET** /api/v1/stock-indices/available | Get available indices |
| [**getStockIndices**](StockIndicesApi.md#getStockIndices) | **GET** /api/v1/stock-indices/{indexSymbol} | Get stock indices data |
| [**getStockIndicesWithHttpInfo**](StockIndicesApi.md#getStockIndicesWithHttpInfo) | **GET** /api/v1/stock-indices/{indexSymbol} | Get stock indices data |
| [**getStockIndicesBatch**](StockIndicesApi.md#getStockIndicesBatch) | **POST** /api/v1/stock-indices/batch | Get multiple stock indices |
| [**getStockIndicesBatchWithHttpInfo**](StockIndicesApi.md#getStockIndicesBatchWithHttpInfo) | **POST** /api/v1/stock-indices/batch | Get multiple stock indices |



## getAvailableIndices

> Map<String, List<String>> getAvailableIndices()

Get available indices

Returns list of available broad market and sector indices

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.StockIndicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        StockIndicesApi apiInstance = new StockIndicesApi(defaultClient);
        try {
            Map<String, List<String>> result = apiInstance.getAvailableIndices();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling StockIndicesApi#getAvailableIndices");
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

[**Map&lt;String, List&lt;String&gt;&gt;**](List.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getAvailableIndicesWithHttpInfo

> ApiResponse<Map<String, List<String>>> getAvailableIndices getAvailableIndicesWithHttpInfo()

Get available indices

Returns list of available broad market and sector indices

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.StockIndicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        StockIndicesApi apiInstance = new StockIndicesApi(defaultClient);
        try {
            ApiResponse<Map<String, List<String>>> response = apiInstance.getAvailableIndicesWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling StockIndicesApi#getAvailableIndices");
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

ApiResponse<[**Map&lt;String, List&lt;String&gt;&gt;**](List.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getStockIndices

> NSEStockIndicesDataV1 getStockIndices(indexSymbol, forceRefresh)

Get stock indices data

Returns constituent stocks for a specific index

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.StockIndicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        StockIndicesApi apiInstance = new StockIndicesApi(defaultClient);
        String indexSymbol = "indexSymbol_example"; // String | 
        Boolean forceRefresh = false; // Boolean | 
        try {
            NSEStockIndicesDataV1 result = apiInstance.getStockIndices(indexSymbol, forceRefresh);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling StockIndicesApi#getStockIndices");
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
| **indexSymbol** | **String**|  | |
| **forceRefresh** | **Boolean**|  | [optional] [default to false] |

### Return type

[**NSEStockIndicesDataV1**](NSEStockIndicesDataV1.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getStockIndicesWithHttpInfo

> ApiResponse<NSEStockIndicesDataV1> getStockIndices getStockIndicesWithHttpInfo(indexSymbol, forceRefresh)

Get stock indices data

Returns constituent stocks for a specific index

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.StockIndicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        StockIndicesApi apiInstance = new StockIndicesApi(defaultClient);
        String indexSymbol = "indexSymbol_example"; // String | 
        Boolean forceRefresh = false; // Boolean | 
        try {
            ApiResponse<NSEStockIndicesDataV1> response = apiInstance.getStockIndicesWithHttpInfo(indexSymbol, forceRefresh);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling StockIndicesApi#getStockIndices");
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
| **indexSymbol** | **String**|  | |
| **forceRefresh** | **Boolean**|  | [optional] [default to false] |

### Return type

ApiResponse<[**NSEStockIndicesDataV1**](NSEStockIndicesDataV1.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getStockIndicesBatch

> List<NSEStockIndicesDataV1> getStockIndicesBatch(requestBody, forceRefresh)

Get multiple stock indices

Returns constituent stocks for multiple indices

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.StockIndicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        StockIndicesApi apiInstance = new StockIndicesApi(defaultClient);
        List<String> requestBody = Arrays.asList(); // List<String> | 
        Boolean forceRefresh = false; // Boolean | 
        try {
            List<NSEStockIndicesDataV1> result = apiInstance.getStockIndicesBatch(requestBody, forceRefresh);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling StockIndicesApi#getStockIndicesBatch");
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
| **requestBody** | [**List&lt;String&gt;**](String.md)|  | |
| **forceRefresh** | **Boolean**|  | [optional] [default to false] |

### Return type

[**List&lt;NSEStockIndicesDataV1&gt;**](NSEStockIndicesDataV1.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getStockIndicesBatchWithHttpInfo

> ApiResponse<List<NSEStockIndicesDataV1>> getStockIndicesBatch getStockIndicesBatchWithHttpInfo(requestBody, forceRefresh)

Get multiple stock indices

Returns constituent stocks for multiple indices

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.StockIndicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        StockIndicesApi apiInstance = new StockIndicesApi(defaultClient);
        List<String> requestBody = Arrays.asList(); // List<String> | 
        Boolean forceRefresh = false; // Boolean | 
        try {
            ApiResponse<List<NSEStockIndicesDataV1>> response = apiInstance.getStockIndicesBatchWithHttpInfo(requestBody, forceRefresh);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling StockIndicesApi#getStockIndicesBatch");
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
| **requestBody** | [**List&lt;String&gt;**](String.md)|  | |
| **forceRefresh** | **Boolean**|  | [optional] [default to false] |

### Return type

ApiResponse<[**List&lt;NSEStockIndicesDataV1&gt;**](NSEStockIndicesDataV1.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

