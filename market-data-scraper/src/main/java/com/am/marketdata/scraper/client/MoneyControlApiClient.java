package com.am.marketdata.scraper.client;


import com.am.marketdata.scraper.model.MoneyControlEtfHoldingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Component
public class MoneyControlApiClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MoneyControlApiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public MoneyControlEtfHoldingResponse fetchEtfHoldings(String isin) {
        String endpoint = String.format("https://mf.moneycontrol.com/service/etf/v1/getSchemeHoldingData?isin=%s&key=Stocks", isin);
        return executeApiCall(endpoint);
    }

    private MoneyControlEtfHoldingResponse executeApiCall(String url) {
        HttpHeaders headers = createBasicHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            if (response.getBody() == null) {
                throw new RuntimeException("Empty response from MoneyControl API");
            }
            return objectMapper.readValue(response.getBody(), MoneyControlEtfHoldingResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Error calling MoneyControl API: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createBasicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");
        headers.set(HttpHeaders.ACCEPT, org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
