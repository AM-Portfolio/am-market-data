package com.marketdata.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marketdata.common.dto.HistoricalDataRequest;
import com.marketdata.common.model.HistoricalDataResponseV1;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Java Client for Market Data API.
 */
public class MarketDataClient {

    private static final Logger log = LoggerFactory.getLogger(MarketDataClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String baseUrl;
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    /**
     * @param baseUrl The base URL of the Market Data API (e.g., "http://localhost:8080")
     */
    public MarketDataClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    /**
     * Get quotes for symbols.
     *
     * @param symbols Comma-separated list of symbols
     * @param timeFrame Timeframe string (e.g., "1D", "5m")
     * @return Map of quotes
     * @throws IOException If the request fails
     */
    public Map<String, Object> getQuotes(String symbols, String timeFrame) throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl + "/api/v1/market-data/quotes").newBuilder()
                .addQueryParameter("symbols", symbols)
                .addQueryParameter("timeFrame", timeFrame)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return mapper.readValue(response.body().string(), new TypeReference<Map<String, Object>>() {});
        }
    }

    /**
     * Get historic data for analysis.
     *
     * @param request Data request object (from common lib)
     * @return Historical Data Response
     * @throws IOException If the request fails
     */
    public HistoricalDataResponseV1 getHistoricalData(HistoricalDataRequest request) throws IOException {
        String json = mapper.writeValueAsString(request);
        RequestBody body = RequestBody.create(json, JSON);

        Request httpRequest = new Request.Builder()
                .url(baseUrl + "/api/v1/market-data/historical-data")
                .post(body)
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return mapper.readValue(response.body().string(), HistoricalDataResponseV1.class);
        }
    }
}
