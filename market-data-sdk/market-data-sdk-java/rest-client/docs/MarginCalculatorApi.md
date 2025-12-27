# MarginCalculatorApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**calculateMargin**](MarginCalculatorApi.md#calculateMargin) | **POST** /api/v1/margin/calculate | Calculate margin requirements |
| [**calculateMarginWithHttpInfo**](MarginCalculatorApi.md#calculateMarginWithHttpInfo) | **POST** /api/v1/margin/calculate | Calculate margin requirements |



## calculateMargin

> MarginCalculationResponse calculateMargin(marginCalculationRequest)

Calculate margin requirements

Calculates SPAN, exposure, and total margin requirements for various positions

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarginCalculatorApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarginCalculatorApi apiInstance = new MarginCalculatorApi(defaultClient);
        MarginCalculationRequest marginCalculationRequest = new MarginCalculationRequest(); // MarginCalculationRequest | 
        try {
            MarginCalculationResponse result = apiInstance.calculateMargin(marginCalculationRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling MarginCalculatorApi#calculateMargin");
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
| **marginCalculationRequest** | [**MarginCalculationRequest**](MarginCalculationRequest.md)|  | |

### Return type

[**MarginCalculationResponse**](MarginCalculationResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## calculateMarginWithHttpInfo

> ApiResponse<MarginCalculationResponse> calculateMargin calculateMarginWithHttpInfo(marginCalculationRequest)

Calculate margin requirements

Calculates SPAN, exposure, and total margin requirements for various positions

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.MarginCalculatorApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        MarginCalculatorApi apiInstance = new MarginCalculatorApi(defaultClient);
        MarginCalculationRequest marginCalculationRequest = new MarginCalculationRequest(); // MarginCalculationRequest | 
        try {
            ApiResponse<MarginCalculationResponse> response = apiInstance.calculateMarginWithHttpInfo(marginCalculationRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling MarginCalculatorApi#calculateMargin");
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
| **marginCalculationRequest** | [**MarginCalculationRequest**](MarginCalculationRequest.md)|  | |

### Return type

ApiResponse<[**MarginCalculationResponse**](MarginCalculationResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

