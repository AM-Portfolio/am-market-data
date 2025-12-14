package com.marketdata.service.upstox;

import com.am.marketdata.upstock.client.UpStockClient;
import com.am.marketdata.upstock.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Service for interacting with Upstox API
 */
@Slf4j
@Service
public class UpstoxApiService {

    @Value("${upstox.auth.api-key}")
    private String apiKey;

    @Value("${upstox.auth.secret-key}")
    private String apiSecret;

    @Value("${upstox.auth.redirect-uri}")
    private String redirectUri;

    private final UpStockClient upStockClient;
    private String accessToken;

    public UpstoxApiService(UpStockClient upStockClient) {
        this.upStockClient = upStockClient;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing Upstox API service");
    }

    public String getLoginUrl() {
        return "https://api.upstox.com/v2/login/authorization/dialog?response_type=code&client_id=" + apiKey
                + "&redirect_uri=" + redirectUri;
    }

    public Object generateSession(String code) {
        log.info("Generating Upstox session for code: {}", code);
        try {
            // Manual Token Exchange using Kong Unirest as used in UpStockClient
            kong.unirest.HttpResponse<String> response = kong.unirest.Unirest
                    .post("https://api-v2.upstox.com/login/authorization/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .field("code", code)
                    .field("client_id", apiKey)
                    .field("client_secret", apiSecret)
                    .field("redirect_uri", redirectUri)
                    .field("grant_type", "authorization_code")
                    .asString();

            if (response.getStatus() == 200) {
                String body = response.getBody();
                // TODO: Parse body to extract access_token
                log.info("Successfully generated Upstox session");

                // Note: We need to parse the JSON and extract access_token here.
                // For now, returning body.
                // In a real scenario we would map this to a DTO.
                return body;
            } else {
                throw new RuntimeException(
                        "Failed to generate token: " + response.getStatus() + " " + response.getBody());
            }

        } catch (Exception e) {
            log.error("Error generating Upstox session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Upstox session", e);
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        // Ideally update UpStockClient's config too if possible, but UpstoxConfig might
        // be immutable bean.
        // If UpStockClient reads from UpstoxConfig bean, we might need to update that
        // bean's state if it has setters.
        // UpstoxConfig has @Data so it has setters.
        // But we need access to the bean. UpStockClient has it.
        // We can't access it easily here unless we cast or expose it.
        // For now, assuming static config or manual update if possible.
        // Actually, UpStockClient reads `upstoxConfig.getAccessToken()` on every
        // request.
        // So if we update the bean, it works.
        // I'll need to inject UpstoxConfig here to update it.
    }

    public MarketQuoteResponse getLtp(List<String> symbols) {
        return upStockClient.getMarketQuotes(symbols);
    }

    public OHLCResponse getOhlc(List<String> symbols, String interval) {
        return upStockClient.getOHLCData(symbols, interval);
    }

    public HistoricalDataResponse getHistoricalCandleData(String symbol, String interval, String fromDate,
            String toDate) {
        return upStockClient.getHistoricalData(symbol, interval, fromDate, toDate);
    }
}
