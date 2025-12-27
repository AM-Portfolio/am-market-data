# MarketAnalyticsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getMarketBreadth**](MarketAnalyticsApi.md#getMarketBreadth) | **GET** /api/v1/analytics/breadth | Get market breadth |
| [**getMarketBreadthWithHttpInfo**](MarketAnalyticsApi.md#getMarketBreadthWithHttpInfo) | **GET** /api/v1/analytics/breadth | Get market breadth |
| [**getMarketSummary**](MarketAnalyticsApi.md#getMarketSummary) | **GET** /api/v1/analytics/summary | Get market summary |
| [**getMarketSummaryWithHttpInfo**](MarketAnalyticsApi.md#getMarketSummaryWithHttpInfo) | **GET** /api/v1/analytics/summary | Get market summary |
| [**getSectorPerformance**](MarketAnalyticsApi.md#getSectorPerformance) | **GET** /api/v1/analytics/sectors | Get sector performance |
| [**getSectorPerformanceWithHttpInfo**](MarketAnalyticsApi.md#getSectorPerformanceWithHttpInfo) | **GET** /api/v1/analytics/sectors | Get sector performance |
| [**getTopMovers**](MarketAnalyticsApi.md#getTopMovers) | **GET** /api/v1/analytics/movers | Get top gainers and losers |
| [**getTopMoversWithHttpInfo**](MarketAnalyticsApi.md#getTopMoversWithHttpInfo) | **GET** /api/v1/analytics/movers | Get top gainers and losers |



## getMarketBreadth

> Map<String, Object> getMarketBreadth()

Get market breadth

Returns market breadth data (advances, declines, unchanged)

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketAnalyticsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketAnalyticsApi apiInstance = new MarketAnalyticsApi(defaultClient);
        try {
            Map<String, Object> result = apiInstance.getMarketBreadth();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketAnalyticsApi#getMarketBreadth");
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

**Map&lt;String, Object&gt;**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getMarketBreadthWithHttpInfo

> ApiResponse<Map<String, Object>> getMarketBreadth getMarketBreadthWithHttpInfo()

Get market breadth

Returns market breadth data (advances, declines, unchanged)

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketAnalyticsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketAnalyticsApi apiInstance = new MarketAnalyticsApi(defaultClient);
        try {
            ApiResponse<Map<String, Object>> response = apiInstance.getMarketBreadthWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketAnalyticsApi#getMarketBreadth");
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

ApiResponse<**Map&lt;String, Object&gt;**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getMarketSummary

> Map<String, Object> getMarketSummary()

Get market summary

Returns overall market summary with key metrics

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketAnalyticsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketAnalyticsApi apiInstance = new MarketAnalyticsApi(defaultClient);
        try {
            Map<String, Object> result = apiInstance.getMarketSummary();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketAnalyticsApi#getMarketSummary");
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

**Map&lt;String, Object&gt;**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getMarketSummaryWithHttpInfo

> ApiResponse<Map<String, Object>> getMarketSummary getMarketSummaryWithHttpInfo()

Get market summary

Returns overall market summary with key metrics

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketAnalyticsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketAnalyticsApi apiInstance = new MarketAnalyticsApi(defaultClient);
        try {
            ApiResponse<Map<String, Object>> response = apiInstance.getMarketSummaryWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketAnalyticsApi#getMarketSummary");
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

ApiResponse<**Map&lt;String, Object&gt;**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getSectorPerformance

> Map<String, Object> getSectorPerformance()

Get sector performance

Returns sector-wise performance data

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketAnalyticsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketAnalyticsApi apiInstance = new MarketAnalyticsApi(defaultClient);
        try {
            Map<String, Object> result = apiInstance.getSectorPerformance();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketAnalyticsApi#getSectorPerformance");
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

**Map&lt;String, Object&gt;**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getSectorPerformanceWithHttpInfo

> ApiResponse<Map<String, Object>> getSectorPerformance getSectorPerformanceWithHttpInfo()

Get sector performance

Returns sector-wise performance data

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketAnalyticsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketAnalyticsApi apiInstance = new MarketAnalyticsApi(defaultClient);
        try {
            ApiResponse<Map<String, Object>> response = apiInstance.getSectorPerformanceWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketAnalyticsApi#getSectorPerformance");
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

ApiResponse<**Map&lt;String, Object&gt;**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getTopMovers

> Map<String, List<Object>> getTopMovers(limit)

Get top gainers and losers

Returns top gaining and losing stocks

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketAnalyticsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketAnalyticsApi apiInstance = new MarketAnalyticsApi(defaultClient);
        Integer limit = 10; // Integer | 
        try {
            Map<String, List<Object>> result = apiInstance.getTopMovers(limit);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketAnalyticsApi#getTopMovers");
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
| **limit** | **Integer**|  | [optional] [default to 10] |

### Return type

[**Map&lt;String, List&lt;Object&gt;&gt;**](List.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getTopMoversWithHttpInfo

> ApiResponse<Map<String, List<Object>>> getTopMovers getTopMoversWithHttpInfo(limit)

Get top gainers and losers

Returns top gaining and losing stocks

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketAnalyticsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketAnalyticsApi apiInstance = new MarketAnalyticsApi(defaultClient);
        Integer limit = 10; // Integer | 
        try {
            ApiResponse<Map<String, List<Object>>> response = apiInstance.getTopMoversWithHttpInfo(limit);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketAnalyticsApi#getTopMovers");
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
| **limit** | **Integer**|  | [optional] [default to 10] |

### Return type

ApiResponse<[**Map&lt;String, List&lt;Object&gt;&gt;**](List.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

