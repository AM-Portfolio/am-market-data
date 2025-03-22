package com.am.marketdata.scraper.exception;

import org.springframework.http.HttpStatusCode;

public class NSEApiException extends MarketDataException {
    private final String endpoint;
    private final HttpStatusCode statusCode;
    private final String responseBody;

    public NSEApiException(String endpoint, HttpStatusCode statusCode, String responseBody, String message) {
        super(String.format("NSE API error [%s] %s - %s: %s", endpoint, statusCode, message, responseBody));
        this.endpoint = endpoint;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public NSEApiException(String endpoint, HttpStatusCode statusCode, String responseBody, String message, Throwable cause) {
        super(String.format("NSE API error [%s] %s - %s: %s", endpoint, statusCode, message, responseBody), cause);
        this.endpoint = endpoint;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Get the error type based on the status code.
     * @return The error type as a string
     */
    public String getErrorType() {
        if (statusCode == null) {
            return "unexpected_error";
        }
        int status = statusCode.value();
        if (status == 401) {
            return "unauthorized";
        } else if (status >= 400 && status < 500) {
            return "client_error";
        } else if (status >= 500) {
            return "server_error";
        }
        return "unexpected_error";
    }
}
