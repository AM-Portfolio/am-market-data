package com.am.marketdata.scraper.client;

import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.common.model.NseETFResponse;
import com.am.marketdata.scraper.service.CookieCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
@Slf4j
public class NSEApiClient {
    @Qualifier("nseApiRestTemplate")
    private final RestTemplate restTemplate;
    private final CookieCacheService cookieCacheService;
    private final ObjectMapper objectMapper;

    @Value("${nse.api.base-url:https://www.nseindia.com}")
    private String baseUrl;

    public NseETFResponse getETFs() {
        String cookies = cookieCacheService.getCookies();
        if (cookies == null) {
            throw new RuntimeException("No valid cookies found in cache");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookies);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = baseUrl + "/api/etf";
        
        try {
            log.info("Calling NSE API for ETFs: {}", url);
            ResponseEntity<NseETFResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NseETFResponse.class
            );
            
            if (response.getBody() == null) {
                throw new RuntimeException("Empty response from NSE API");
            }

            // Log response details
            NseETFResponse etfs = response.getBody();
            try {
                log.info("NSE API Response - Status: {}, Body: {}", 
                    response.getStatusCode(),
                    objectMapper.writeValueAsString(etfs)
                );
            } catch (Exception e) {
                log.warn("Failed to serialize response for logging", e);
            }

            // Log summary of ETFs
            if (etfs.getData() != null) {
                log.info("Fetched {} ETFs. First ETF: {}", 
                    etfs.getData().size(),
                    etfs.getData().isEmpty() ? "none" : 
                        String.format("%s (Last: %.2f, Change: %.2f%%)", 
                            etfs.getData().get(0).getSymbol(),
                            etfs.getData().get(0).getLastTradedPrice(),
                            etfs.getData().get(0).getPercentChange()
                        )
                );
            }
            
            return etfs;
            
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Unauthorized access to NSE API. Cookies might be expired. Response: {}", e.getResponseBodyAsString());
            cookieCacheService.invalidateCookies();
            throw new RuntimeException("Unauthorized access to NSE API", e);
        } catch (Exception e) {
            log.error("Error calling NSE API: {}", url, e);
            throw new RuntimeException("Failed to call NSE API", e);
        }
    }

    public NSEIndicesResponse getAllIndices() {
        String cookies = cookieCacheService.getCookies();
        if (cookies == null) {
            throw new RuntimeException("No valid cookies found in cache");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookies);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = baseUrl + "/api/allIndices";
        
        try {
            log.info("Calling NSE API: {}", url);
            ResponseEntity<NSEIndicesResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NSEIndicesResponse.class
            );
            
            if (response.getBody() == null) {
                throw new RuntimeException("Empty response from NSE API");
            }

            // Log response details
            NSEIndicesResponse indices = response.getBody();
            try {
                log.info("NSE API Response - Status: {}, Body: {}", 
                    response.getStatusCode(),
                    objectMapper.writeValueAsString(indices)
                );
            } catch (Exception e) {
                log.warn("Failed to serialize response for logging", e);
            }

            // Log summary of indices
            if (indices.getData() != null) {
                log.info("Fetched {} indices. First index: {}", 
                    indices.getData().size(),
                    indices.getData().isEmpty() ? "none" : 
                        String.format("%s (Last: %.2f, Change: %.2f%%)", 
                            indices.getData().get(0).getIndexSymbol(),
                            indices.getData().get(0).getLast(),
                            indices.getData().get(0).getPercentChange()
                        )
                );
            }
            
            return indices;
            
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Unauthorized access to NSE API. Cookies might be expired. Response: {}", e.getResponseBodyAsString());
            cookieCacheService.invalidateCookies();
            throw new RuntimeException("Unauthorized access to NSE API", e);
        } catch (Exception e) {
            log.error("Error calling NSE API: {}", url, e);
            throw new RuntimeException("Failed to call NSE API", e);
        }
    }
}
