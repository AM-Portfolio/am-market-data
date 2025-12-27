# BrokerageCalculatorApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**calculateBrokerage**](BrokerageCalculatorApi.md#calculateBrokerage) | **POST** /api/v1/brokerage/calculate | Calculate brokerage and charges |
| [**calculateBrokerageWithHttpInfo**](BrokerageCalculatorApi.md#calculateBrokerageWithHttpInfo) | **POST** /api/v1/brokerage/calculate | Calculate brokerage and charges |



## calculateBrokerage

> BrokerageCalculationResponse calculateBrokerage(brokerageCalculationRequest)

Calculate brokerage and charges

Calculates total brokerage, taxes, and other charges based on trade details

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.BrokerageCalculatorApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        BrokerageCalculatorApi apiInstance = new BrokerageCalculatorApi(defaultClient);
        BrokerageCalculationRequest brokerageCalculationRequest = new BrokerageCalculationRequest(); // BrokerageCalculationRequest | 
        try {
            BrokerageCalculationResponse result = apiInstance.calculateBrokerage(brokerageCalculationRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling BrokerageCalculatorApi#calculateBrokerage");
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
| **brokerageCalculationRequest** | [**BrokerageCalculationRequest**](BrokerageCalculationRequest.md)|  | |

### Return type

[**BrokerageCalculationResponse**](BrokerageCalculationResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

## calculateBrokerageWithHttpInfo

> ApiResponse<BrokerageCalculationResponse> calculateBrokerage calculateBrokerageWithHttpInfo(brokerageCalculationRequest)

Calculate brokerage and charges

Calculates total brokerage, taxes, and other charges based on trade details

### Example

```java
// Import classes:
import com.am.marketdata.client.ApiClient;
import com.am.marketdata.client.ApiException;
import com.am.marketdata.client.ApiResponse;
import com.am.marketdata.client.Configuration;
import com.am.marketdata.client.models.*;
import com.am.marketdata.api.BrokerageCalculatorApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        BrokerageCalculatorApi apiInstance = new BrokerageCalculatorApi(defaultClient);
        BrokerageCalculationRequest brokerageCalculationRequest = new BrokerageCalculationRequest(); // BrokerageCalculationRequest | 
        try {
            ApiResponse<BrokerageCalculationResponse> response = apiInstance.calculateBrokerageWithHttpInfo(brokerageCalculationRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling BrokerageCalculatorApi#calculateBrokerage");
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
| **brokerageCalculationRequest** | [**BrokerageCalculationRequest**](BrokerageCalculationRequest.md)|  | |

### Return type

ApiResponse<[**BrokerageCalculationResponse**](BrokerageCalculationResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

