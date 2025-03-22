package com.am.marketdata.scraper.client;

import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.common.model.NseETFResponse;
import com.am.marketdata.scraper.client.api.NSEApi;
import com.am.marketdata.scraper.client.executor.RequestExecutor;
import com.am.marketdata.scraper.client.handler.ETFRequestHandler;
import com.am.marketdata.scraper.client.handler.IndicesRequestHandler;
import com.am.marketdata.scraper.client.handler.StockIndicesRequestHandler;
import com.am.marketdata.scraper.exception.NSEApiException;
import com.am.marketdata.scraper.service.cookie.CookieCacheService;
import com.am.marketdata.scraper.service.cookie.CookieScraperService;
import com.am.marketdata.scraper.model.WebsiteCookies;
import com.am.marketdata.scraper.model.CookieInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of NSE API client
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NSEApiClientImpl implements NSEApi {

    private static final String ETF_ENDPOINT = "/api/etf";
    private static final String INDICES_ENDPOINT = "/api/allIndices";
    private static final String STOCK_INDICES_ENDPOINT = "/api/equity-stockIndices";

    private final RequestExecutor requestExecutor;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final CookieCacheService cookieCacheService;
    private final CookieScraperService cookieScraperService;

    @Autowired
    @Qualifier("nseRestTemplate")
    private RestTemplate restTemplate;

    @Value("${nse.api.base-url:https://www.nseindia.com}")
    private String baseUrl;

    private ETFRequestHandler etfRequestHandler;
    private IndicesRequestHandler indicesRequestHandler;
    private StockIndicesRequestHandler stockIndicesRequestHandler;

    @PostConstruct
    public void init() {
        log.info("Initializing NSE API Client with base URL: {}", baseUrl);
        
        etfRequestHandler = new ETFRequestHandler(objectMapper);
        indicesRequestHandler = new IndicesRequestHandler(objectMapper);
        stockIndicesRequestHandler = new StockIndicesRequestHandler(objectMapper);
    }

    @Override
    public NseETFResponse getETFs() throws NSEApiException {
        return requestExecutor.execute(etfRequestHandler);
    }

    @Override
    public NSEStockInsidicesData getStockbyInsidices(String index) throws NSEApiException {
        return requestExecutor.execute(
            stockIndicesRequestHandler.withIndexSymbol(index)
        );
    }

    @Override
    public NSEIndicesResponse getAllIndices() throws NSEApiException {
        return requestExecutor.execute(indicesRequestHandler);
    }

    @Override
    public HttpHeaders fetchCookies() throws NSEApiException {
        try {
            // First try to get cookies from cache
            String cachedCookies = cookieCacheService.getCookies();
            
            if (cachedCookies != null) {
                log.info("Found cookies in cache");
                
                // Check if cookies are expired
                HttpHeaders cachedHeaders = new HttpHeaders();
                cachedHeaders.set(HttpHeaders.COOKIE, cachedCookies);
                
                // Try to use cached cookies by making a test request
                try {
                    ResponseEntity<String> testResponse = restTemplate.exchange(
                        baseUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(cachedHeaders),
                        String.class
                    );
                    
                    if (testResponse.getStatusCode().is2xxSuccessful()) {
                        log.info("Cached cookies are valid");
                        return cachedHeaders;
                    }
                } catch (Exception e) {
                    log.warn("Cached cookies are invalid, will fetch fresh cookies");
                }
            }
            
            // If no valid cached cookies, fetch fresh cookies using scraper
            log.info("Fetching fresh cookies from scraper service");
            WebsiteCookies freshCookies = cookieScraperService.scrapeCookies(baseUrl);
            
            if (freshCookies != null && freshCookies.getCookies() != null) {
                // Store fresh cookies in cache
                String cookiesString = freshCookies.generateCookiesString();
                cookieCacheService.storeCookies(cookiesString);
                
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.COOKIE, cookiesString);
                log.info("Successfully fetched and cached fresh cookies");
                
                // Log each cookie
                List<CookieInfo> cookies = freshCookies.getCookies();
                log.info("Cookie count: {}", cookies.size());
                cookies.forEach(cookie -> log.info("Cookie: {}={}", cookie.getName(), cookie.getValue()));
                
                return headers;
            } else {
                throw new NSEApiException("homepage", HttpStatus.INTERNAL_SERVER_ERROR, "", "Failed to fetch fresh cookies", null);
            }
        } catch (HttpClientErrorException e) {
            throw new NSEApiException("homepage", e.getStatusCode(), e.getResponseBodyAsString(), "Client error fetching cookies", e);
        } catch (HttpServerErrorException e) {
            throw new NSEApiException("homepage", e.getStatusCode(), e.getResponseBodyAsString(), "Server error fetching cookies", e);
        } catch (ResourceAccessException e) {
            throw new NSEApiException("homepage", HttpStatus.SERVICE_UNAVAILABLE, "", "Network error fetching cookies", e);
        } catch (Exception e) {
            throw new NSEApiException("homepage", HttpStatus.INTERNAL_SERVER_ERROR, "", "Error fetching cookies", e);
        }
    }

    private HttpHeaders createBasicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0");
        return headers;
    }
}
