package com.am.marketdata.mutualfund.client;

import com.am.marketdata.common.model.MutualFundData;
import com.am.marketdata.common.model.MutualFundScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MutualFundApiClient {

    private final RestTemplate restTemplate;
    private final MutualFundApiConfig apiConfig;

    public MutualFundData fetchMutualFundData(MutualFundScheme scheme) {
        try {
            log.info("Fetching mutual fund data for scheme: {}", scheme.getCode());
            
            // Construct request URL
            String url = apiConfig.getBaseUrl() + 
                String.format("/scheme/%s/data", scheme.getCode());
            
            // Add query parameters
            Map<String, String> params = Map.of(
                "date", LocalDateTime.now().toString()
            );
            
            // Make API call
            MutualFundData response = restTemplate.getForObject(url, MutualFundData.class, params);
            
            if (response == null) {
                throw new RuntimeException("No data received from API for scheme: " + scheme.getCode());
            }
            
            log.info("Successfully fetched data for scheme: {}", scheme.getCode());
            return response;
            
        } catch (Exception e) {
            log.error("Error fetching mutual fund data for scheme: {}", scheme.getCode(), e);
            throw e;
        }
    }
}
