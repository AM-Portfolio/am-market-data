# MarketDataPollingApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getConnectionStatus**](MarketDataPollingApi.md#getConnectionStatus) | **GET** /api/v1/polling/status | Get connection status |
| [**getConnectionStatusWithHttpInfo**](MarketDataPollingApi.md#getConnectionStatusWithHttpInfo) | **GET** /api/v1/polling/status | Get connection status |
| [**getUpdateSchema**](MarketDataPollingApi.md#getUpdateSchema) | **GET** /api/v1/polling/schema/update | Internal use for SDK generation |
| [**getUpdateSchemaWithHttpInfo**](MarketDataPollingApi.md#getUpdateSchemaWithHttpInfo) | **GET** /api/v1/polling/schema/update | Internal use for SDK generation |
| [**pollMarketData**](MarketDataPollingApi.md#pollMarketData) | **GET** /api/v1/polling/data | Poll market data |
| [**pollMarketDataWithHttpInfo**](MarketDataPollingApi.md#pollMarketDataWithHttpInfo) | **GET** /api/v1/polling/data | Poll market data |
| [**subscribe**](MarketDataPollingApi.md#subscribe) | **POST** /api/v1/polling/subscribe | Subscribe to symbols |
| [**subscribeWithHttpInfo**](MarketDataPollingApi.md#subscribeWithHttpInfo) | **POST** /api/v1/polling/subscribe | Subscribe to symbols |
| [**unsubscribe**](MarketDataPollingApi.md#unsubscribe) | **POST** /api/v1/polling/unsubscribe | Unsubscribe from symbols |
| [**unsubscribeWithHttpInfo**](MarketDataPollingApi.md#unsubscribeWithHttpInfo) | **POST** /api/v1/polling/unsubscribe | Unsubscribe from symbols |



## getConnectionStatus

> Map<String, Object> getConnectionStatus()

Get connection status

Returns streaming connection status

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketDataPollingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketDataPollingApi apiInstance = new MarketDataPollingApi(defaultClient);
        try {
            Map<String, Object> result = apiInstance.getConnectionStatus();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataPollingApi#getConnectionStatus");
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

## getConnectionStatusWithHttpInfo

> ApiResponse<Map<String, Object>> getConnectionStatus getConnectionStatusWithHttpInfo()

Get connection status

Returns streaming connection status

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketDataPollingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketDataPollingApi apiInstance = new MarketDataPollingApi(defaultClient);
        try {
            ApiResponse<Map<String, Object>> response = apiInstance.getConnectionStatusWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataPollingApi#getConnectionStatus");
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


## getUpdateSchema

> MarketDataUpdateV1 getUpdateSchema()

Internal use for SDK generation

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketDataPollingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketDataPollingApi apiInstance = new MarketDataPollingApi(defaultClient);
        try {
            MarketDataUpdateV1 result = apiInstance.getUpdateSchema();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataPollingApi#getUpdateSchema");
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

[**MarketDataUpdateV1**](MarketDataUpdateV1.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## getUpdateSchemaWithHttpInfo

> ApiResponse<MarketDataUpdateV1> getUpdateSchema getUpdateSchemaWithHttpInfo()

Internal use for SDK generation

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketDataPollingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketDataPollingApi apiInstance = new MarketDataPollingApi(defaultClient);
        try {
            ApiResponse<MarketDataUpdateV1> response = apiInstance.getUpdateSchemaWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataPollingApi#getUpdateSchema");
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

ApiResponse<[**MarketDataUpdateV1**](MarketDataUpdateV1.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## pollMarketData

> Map<String, Object> pollMarketData(symbols, timeFrame, indexSymbol)

Poll market data

Poll live market data for given symbols

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketDataPollingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketDataPollingApi apiInstance = new MarketDataPollingApi(defaultClient);
        String symbols = "symbols_example"; // String | 
        String timeFrame = "1m"; // String | 
        Boolean indexSymbol = false; // Boolean | 
        try {
            Map<String, Object> result = apiInstance.pollMarketData(symbols, timeFrame, indexSymbol);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataPollingApi#pollMarketData");
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
| **timeFrame** | **String**|  | [optional] [default to 1m] |
| **indexSymbol** | **Boolean**|  | [optional] [default to false] |

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

## pollMarketDataWithHttpInfo

> ApiResponse<Map<String, Object>> pollMarketData pollMarketDataWithHttpInfo(symbols, timeFrame, indexSymbol)

Poll market data

Poll live market data for given symbols

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketDataPollingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketDataPollingApi apiInstance = new MarketDataPollingApi(defaultClient);
        String symbols = "symbols_example"; // String | 
        String timeFrame = "1m"; // String | 
        Boolean indexSymbol = false; // Boolean | 
        try {
            ApiResponse<Map<String, Object>> response = apiInstance.pollMarketDataWithHttpInfo(symbols, timeFrame, indexSymbol);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataPollingApi#pollMarketData");
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
| **timeFrame** | **String**|  | [optional] [default to 1m] |
| **indexSymbol** | **Boolean**|  | [optional] [default to false] |

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


## subscribe

> Map<String, Object> subscribe(requestBody)

Subscribe to symbols

Subscribe to symbols for real-time updates

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketDataPollingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketDataPollingApi apiInstance = new MarketDataPollingApi(defaultClient);
        List<String> requestBody = Arrays.asList(); // List<String> | 
        try {
            Map<String, Object> result = apiInstance.subscribe(requestBody);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataPollingApi#subscribe");
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

### Return type

**Map&lt;String, Object&gt;**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## subscribeWithHttpInfo

> ApiResponse<Map<String, Object>> subscribe subscribeWithHttpInfo(requestBody)

Subscribe to symbols

Subscribe to symbols for real-time updates

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketDataPollingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketDataPollingApi apiInstance = new MarketDataPollingApi(defaultClient);
        List<String> requestBody = Arrays.asList(); // List<String> | 
        try {
            ApiResponse<Map<String, Object>> response = apiInstance.subscribeWithHttpInfo(requestBody);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataPollingApi#subscribe");
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

### Return type

ApiResponse<**Map&lt;String, Object&gt;**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## unsubscribe

> Map<String, Object> unsubscribe(requestBody)

Unsubscribe from symbols

Unsubscribe from symbols

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketDataPollingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketDataPollingApi apiInstance = new MarketDataPollingApi(defaultClient);
        List<String> requestBody = Arrays.asList(); // List<String> | 
        try {
            Map<String, Object> result = apiInstance.unsubscribe(requestBody);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataPollingApi#unsubscribe");
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

### Return type

**Map&lt;String, Object&gt;**


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## unsubscribeWithHttpInfo

> ApiResponse<Map<String, Object>> unsubscribe unsubscribeWithHttpInfo(requestBody)

Unsubscribe from symbols

Unsubscribe from symbols

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarketDataPollingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarketDataPollingApi apiInstance = new MarketDataPollingApi(defaultClient);
        List<String> requestBody = Arrays.asList(); // List<String> | 
        try {
            ApiResponse<Map<String, Object>> response = apiInstance.unsubscribeWithHttpInfo(requestBody);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarketDataPollingApi#unsubscribe");
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

### Return type

ApiResponse<**Map&lt;String, Object&gt;**>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

