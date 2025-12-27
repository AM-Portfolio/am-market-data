package com.am.marketdata.provider.zerodha.exception;

public class ZerodhaApiException extends RuntimeException {
    public ZerodhaApiException(String message) {
        super(message);
    }
    public ZerodhaApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
