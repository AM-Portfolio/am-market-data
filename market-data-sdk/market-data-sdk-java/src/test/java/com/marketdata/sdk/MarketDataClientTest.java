package com.marketdata.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketDataClientTest {

    private MarketDataClient marketDataClient;

    @Mock
    private OkHttpClient mockHttpClient;

    @Mock
    private Call mockCall;

    @BeforeEach
    void setUp() throws Exception {
        marketDataClient = new MarketDataClient("http://localhost:8080");

        // Inject mock HttpClient using reflection since it's private final
        Field clientField = MarketDataClient.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(marketDataClient, mockHttpClient);
    }

    @Test
    void testGetQuotes_Success() throws IOException {
        // Arrange
        String jsonResponse = "{\"NSE:RELIANCE\": {\"lastPrice\": 2500.0}}";
        Response response = new Response.Builder()
                .request(new Request.Builder().url("http://localhost:8080").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(jsonResponse, MediaType.get("application/json")))
                .build();

        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        // Act
        Map<String, Object> quotes = marketDataClient.getQuotes("NSE:RELIANCE", "1D");

        // Assert
        assertNotNull(quotes);
        assertTrue(quotes.containsKey("NSE:RELIANCE"));
    }
}
