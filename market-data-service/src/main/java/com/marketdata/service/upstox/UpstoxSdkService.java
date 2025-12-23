package com.marketdata.service.upstox;

import com.am.marketdata.upstock.config.UpstoxConfig;
import com.upstox.ApiClient;
import com.upstox.ApiException;
import com.upstox.auth.OAuth;
import io.swagger.client.api.MarketQuoteV3Api;
import com.upstox.api.GetMarketQuoteLastTradedPriceResponseV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Slf4j
public class UpstoxSdkService {

    private final StringRedisTemplate redisTemplate;
    private final UpstoxConfig upstoxConfig;
    private String accessToken;

    private static final String REDIS_KEY_ACCESS_TOKEN = "market_data:upstox:access_token";

    public UpstoxSdkService(StringRedisTemplate redisTemplate, UpstoxConfig upstoxConfig) {
        this.redisTemplate = redisTemplate;
        this.upstoxConfig = upstoxConfig;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing Upstox SDK Service");
        try {
            // Try to load cached token from Redis
            String cachedToken = redisTemplate.opsForValue().get(REDIS_KEY_ACCESS_TOKEN);
            if (cachedToken != null && !cachedToken.isEmpty()) {
                log.info("Found cached Access Token in Redis for SDK Service");
                this.setAccessToken(cachedToken);
            } else {
                log.info("No cached Access Token found in Redis for SDK Service, checking configuration");
                if (upstoxConfig.getAccessToken() != null && !upstoxConfig.getAccessToken().isEmpty()) {
                    log.info("Found Access Token in configuration for SDK Service");
                    this.setAccessToken(upstoxConfig.getAccessToken());
                } else {
                    log.warn("No Access Token found for SDK Service");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to initialize SDK Service token from Redis: {}", e.getMessage());
            // Fallback to config
            if (upstoxConfig.getAccessToken() != null && !upstoxConfig.getAccessToken().isEmpty()) {
                this.setAccessToken(upstoxConfig.getAccessToken());
            }
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Fetch LTP for a list of instrument keys using Upstox V3 SDK
     * (MarketQuoteV3Api)
     *
     * @param instrumentKeys List of instrument keys (e.g., "NSE_EQ|INE848E01016")
     * @return GetMarketQuoteLastTradedPriceResponseV3 containing LTP data
     * @throws ApiException if the API call fails
     */
    public GetMarketQuoteLastTradedPriceResponseV3 getLtp(List<String> instrumentKeys)
            throws ApiException {
        if (this.accessToken == null || this.accessToken.isEmpty()) {
            // Try to refresh from config one last time
            if (upstoxConfig.getAccessToken() != null) {
                this.accessToken = upstoxConfig.getAccessToken();
            }
            if (this.accessToken == null || this.accessToken.isEmpty()) {
                throw new IllegalStateException("Upstox Access token is not initialized");
            }
        }

        if (instrumentKeys == null || instrumentKeys.isEmpty()) {
            return new GetMarketQuoteLastTradedPriceResponseV3();
        }

        // Initialize ApiClient
        ApiClient apiClient = new ApiClient();

        // Configure OAuth2 access token
        // Use the auth name "OAUTH2" as per standard generated SDKs
        OAuth oAuth = (OAuth) apiClient.getAuthentication("OAUTH2");
        if (oAuth != null) {
            oAuth.setAccessToken(this.accessToken);
        } else {
            // Fallback if getAuthentication returns null or name differs (though OAUTH2 is
            // standard)
            // Some SDK versions might allow setAccessToken directly on client
            apiClient.setAccessToken(this.accessToken);
        }

        MarketQuoteV3Api marketQuoteV3Api = new MarketQuoteV3Api(apiClient);

        // Join keys with comma
        String symbolList = String.join(",", instrumentKeys);

        log.debug("Calling MarketQuoteV3Api.getLtp with symbols: {}", symbolList);
        return marketQuoteV3Api.getLtp(symbolList);
    }
}
