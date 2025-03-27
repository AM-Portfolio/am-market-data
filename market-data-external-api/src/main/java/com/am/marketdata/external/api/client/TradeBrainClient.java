package com.am.marketdata.external.api.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.am.marketdata.external.api.config.TradeBrainConfig;
import com.am.marketdata.external.api.config.TradeBrainConfig.Analytics;
import com.am.marketdata.external.api.config.TradeBrainConfig.Company;
import com.am.marketdata.external.api.config.TradeBrainConfig.Documents;
import com.am.marketdata.external.api.config.TradeBrainConfig.Financial;
import com.am.marketdata.external.api.config.TradeBrainConfig.Profile;
import com.am.marketdata.external.api.config.TradeBrainConfig.Reports;
import com.am.marketdata.external.api.config.TradeBrainConfig.Stock;
import com.am.marketdata.external.api.model.ApiResponse;
import com.am.marketdata.external.api.registry.ApiEndpoint;
import com.am.marketdata.external.api.registry.ApiEndpointRegistry;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Client for TradeBrain API calls
 */
@Component
@Slf4j
public class TradeBrainClient {
    
    private final ApiClient apiClient;
    private final TradeBrainConfig tradeBrainConfig;
    private final ApiEndpointRegistry endpointRegistry;
    
    // Endpoint ID prefixes
    private static final String TRADEBRAIN_PREFIX = "tradebrain.";
    private static final String COMPANY_PREFIX = TRADEBRAIN_PREFIX + "company.";
    private static final String PROFILE_PREFIX = COMPANY_PREFIX + "profile.";
    private static final String FINANCIAL_PREFIX = COMPANY_PREFIX + "financial.";
    private static final String DOCUMENTS_PREFIX = COMPANY_PREFIX + "documents.";
    private static final String REPORTS_PREFIX = COMPANY_PREFIX + "reports.";
    private static final String STOCK_PREFIX = COMPANY_PREFIX + "stock.";
    private static final String ANALYTICS_PREFIX = COMPANY_PREFIX + "analytics.";
    private static final String INDICES_PREFIX = TRADEBRAIN_PREFIX + "indices.";
    
    // Common endpoint IDs
    public static final String TRADEBRAIN_ENDPOINT_INDEX = INDICES_PREFIX + "all";
    public static final String TRADEBRAIN_STOCK_ENDPOINT_DETAILS = STOCK_PREFIX + "details";
    public static final String TRADEBRAIN_STOCK_ENDPOINT_HISTORICAL = STOCK_PREFIX + "historical";
    public static final String TRADEBRAIN_STOCK_ENDPOINT_TECHNICAL = STOCK_PREFIX + "technical";
    public static final String TRADEBRAIN_STOCK_ENDPOINT_SHAREHOLDING = STOCK_PREFIX + "shareholding";
    public static final String TRADEBRAIN_STOCK_ENDPOINT_HEATMAP = STOCK_PREFIX + "heatmap";
    
    /**
     * Constructor with dependencies
     * 
     * @param apiClient Base API client
     * @param tradeBrainConfig TradeBrain configuration
     * @param endpointRegistry API endpoint registry
     */
    public TradeBrainClient(
            ApiClient apiClient, 
            TradeBrainConfig tradeBrainConfig,
            ApiEndpointRegistry endpointRegistry) {
        this.apiClient = apiClient;
        this.tradeBrainConfig = tradeBrainConfig;
        this.endpointRegistry = endpointRegistry;
    }
    
    /**
     * Initialize the client and register endpoints
     */
    @PostConstruct
    public void init() {
        // Create default headers
        Map<String, String> headers = createDefaultHeaders();
        
        // Register all endpoints
        registerIndicesEndpoints(headers);
        registerCompanyEndpoints(headers);
        
        log.info("TradeBrain API client initialized with base URL: {}", tradeBrainConfig.getBaseUrl());
    }
    
    /**
     * Create default headers for API requests
     * 
     * @return Map of default headers
     */
    private Map<String, String> createDefaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", tradeBrainConfig.getHeaders().getUserAgent());
        headers.put("Accept", tradeBrainConfig.getHeaders().getAccept());
        headers.put("Accept-Language", tradeBrainConfig.getHeaders().getAcceptLanguage());
        return headers;
    }
    
    /**
     * Register indices endpoints
     * 
     * @param headers Default headers
     */
    private void registerIndicesEndpoints(Map<String, String> headers) {
        if (tradeBrainConfig.getApi().getIndices() != null) {
            registerEndpoint(
                TRADEBRAIN_ENDPOINT_INDEX,
                "TradeBrain Indices All",
                tradeBrainConfig.getApi().getIndices().getAll(),
                headers
            );
        }
    }
    
    /**
     * Register all company-related endpoints
     * 
     * @param headers Default headers
     */
    private void registerCompanyEndpoints(Map<String, String> headers) {
        Company company = tradeBrainConfig.getApi().getCompany();
        if (company == null) {
            return;
        }
        
        // Register profile endpoints
        registerProfileEndpoints(company.getProfile(), headers);
        
        // Register financial endpoints
        registerFinancialEndpoints(company.getFinancial(), headers);
        
        // Register documents endpoints
        registerDocumentsEndpoints(company.getDocuments(), headers);
        
        // Register reports endpoints
        registerReportsEndpoints(company.getReports(), headers);
        
        // Register stock endpoints
        registerStockEndpoints(company.getStock(), headers);
        
        // Register analytics endpoints
        registerAnalyticsEndpoints(company.getAnalytics(), headers);
    }
    
    /**
     * Register profile endpoints
     * 
     * @param profile Profile configuration
     * @param headers Default headers
     */
    private void registerProfileEndpoints(Profile profile, Map<String, String> headers) {
        if (profile == null) {
            return;
        }
        
        registerEndpoint(
            PROFILE_PREFIX + "short-details",
            "TradeBrain Profile Short Details",
            profile.getShortDetails(),
            headers
        );
        
        registerEndpoint(
            PROFILE_PREFIX + "company-profile",
            "TradeBrain Profile Company Profile",
            profile.getCompanyProfile(),
            headers
        );
        
        registerEndpoint(
            PROFILE_PREFIX + "company-insights",
            "TradeBrain Profile Company Insights",
            profile.getCompanyInsights(),
            headers
        );
        
        registerEndpoint(
            PROFILE_PREFIX + "key-metrics",
            "TradeBrain Profile Key Metrics",
            profile.getKeymetrics(),
            headers
        );
        
        registerEndpoint(
            PROFILE_PREFIX + "research-report",
            "TradeBrain Profile Research Report",
            profile.getResearchReport(),
            headers
        );
        
        registerEndpoint(
            PROFILE_PREFIX + "corporate-actions",
            "TradeBrain Profile Corporate Actions",
            profile.getCorporateActions(),
            headers
        );
        
        registerEndpoint(
            PROFILE_PREFIX + "top-shareholders",
            "TradeBrain Profile Top Shareholders",
            profile.getTopShareholders(),
            headers
        );
        
        registerEndpoint(
            PROFILE_PREFIX + "company-score",
            "TradeBrain Profile Company Score",
            profile.getCompanyScore(),
            headers
        );
        
        registerEndpoint(
            PROFILE_PREFIX + "board-of-directors",
            "TradeBrain Profile Board of Directors",
            profile.getBoardOfDirectors(),
            headers
        );
    }
    
    /**
     * Register financial endpoints
     * 
     * @param financial Financial configuration
     * @param headers Default headers
     */
    private void registerFinancialEndpoints(Financial financial, Map<String, String> headers) {
        if (financial == null) {
            return;
        }
        
        registerEndpoint(
            FINANCIAL_PREFIX + "dividends",
            "TradeBrain Financial Dividends",
            financial.getDividends(),
            headers
        );
        
        registerEndpoint(
            FINANCIAL_PREFIX + "half-yearly-statement",
            "TradeBrain Financial Half Yearly Statement",
            financial.getHalfYearlyStatement(),
            headers
        );
        
        registerEndpoint(
            FINANCIAL_PREFIX + "quarterly-results-standalone",
            "TradeBrain Financial Quarterly Results Standalone",
            financial.getQuarterlyResultsStandalone(),
            headers
        );
        
        registerEndpoint(
            FINANCIAL_PREFIX + "profit-and-loss-standalone",
            "TradeBrain Financial Profit and Loss Standalone",
            financial.getProfitAndLossStandalone(),
            headers
        );
        
        registerEndpoint(
            FINANCIAL_PREFIX + "balance-sheet-standalone",
            "TradeBrain Financial Balance Sheet Standalone",
            financial.getBalanceSheetStandalone(),
            headers
        );
        
        registerEndpoint(
            FINANCIAL_PREFIX + "cash-flow-standalone",
            "TradeBrain Financial Cash Flow Standalone",
            financial.getCashFlowStandalone(),
            headers
        );
    }
    
    /**
     * Register documents endpoints
     * 
     * @param documents Documents configuration
     * @param headers Default headers
     */
    private void registerDocumentsEndpoints(Documents documents, Map<String, String> headers) {
        if (documents == null) {
            return;
        }
        
        registerEndpoint(
            DOCUMENTS_PREFIX + "credit-rating",
            "TradeBrain Documents Credit Rating",
            documents.getCreditRating(),
            headers
        );
        
        registerEndpoint(
            DOCUMENTS_PREFIX + "industry-peers",
            "TradeBrain Documents Industry Peers",
            documents.getIndustryPeers(),
            headers
        );
    }
    
    /**
     * Register reports endpoints
     * 
     * @param reports Reports configuration
     * @param headers Default headers
     */
    private void registerReportsEndpoints(Reports reports, Map<String, String> headers) {
        if (reports == null) {
            return;
        }
        
        registerEndpoint(
            REPORTS_PREFIX + "director-report",
            "TradeBrain Reports Director Report",
            reports.getDirectorReport(),
            headers
        );
        
        registerEndpoint(
            REPORTS_PREFIX + "chairman-report",
            "TradeBrain Reports Chairman Report",
            reports.getChairmanReport(),
            headers
        );
        
        registerEndpoint(
            REPORTS_PREFIX + "auditor-report",
            "TradeBrain Reports Auditor Report",
            reports.getAuditorReport(),
            headers
        );
        
        registerEndpoint(
            REPORTS_PREFIX + "annual-reports",
            "TradeBrain Reports Annual Reports",
            reports.getAnnualReports(),
            headers
        );
    }
    
    /**
     * Register stock endpoints
     * 
     * @param stock Stock configuration
     * @param headers Default headers
     */
    private void registerStockEndpoints(Stock stock, Map<String, String> headers) {
        if (stock == null) {
            return;
        }
        
        registerEndpoint(
            TRADEBRAIN_STOCK_ENDPOINT_DETAILS,
            "TradeBrain Stock Details",
            stock.getDetails(),
            headers
        );
        
        registerEndpoint(
            TRADEBRAIN_STOCK_ENDPOINT_HISTORICAL,
            "TradeBrain Stock Historical",
            stock.getHistorical(),
            headers
        );
        
        registerEndpoint(
            TRADEBRAIN_STOCK_ENDPOINT_TECHNICAL,
            "TradeBrain Stock Technical",
            stock.getTechnical(),
            headers
        );
        
        registerEndpoint(
            TRADEBRAIN_STOCK_ENDPOINT_SHAREHOLDING,
            "TradeBrain Stock Shareholding",
            stock.getShareholding(),
            headers
        );
        
        registerEndpoint(
            TRADEBRAIN_STOCK_ENDPOINT_HEATMAP,
            "TradeBrain Stock Heatmap",
            stock.getHeatmap(),
            headers
        );
    }
    
    /**
     * Register analytics endpoints
     * 
     * @param analytics Analytics configuration
     * @param headers Default headers
     */
    private void registerAnalyticsEndpoints(Analytics analytics, Map<String, String> headers) {
        if (analytics == null) {
            return;
        }
        
        registerEndpoint(
            ANALYTICS_PREFIX + "analytics",
            "TradeBrain Analytics",
            analytics.getAnalytics(),
            headers
        );
    }
    
    /**
     * Register an endpoint with the registry
     * 
     * @param id Endpoint ID
     * @param name Endpoint name
     * @param path Endpoint path
     * @param headers Default headers
     */
    private void registerEndpoint(String id, String name, String path, Map<String, String> headers) {
        if (path == null || path.isEmpty()) {
            return;
        }
        
        ApiEndpoint endpoint = ApiEndpoint.builder()
                .id(id)
                .name(name)
                .baseUrl(tradeBrainConfig.getBaseUrl())
                .path(path)
                .method("GET")
                .defaultHeaders(headers)
                .healthCheckEnabled(true)
                .build();
        
        endpointRegistry.registerEndpoint(endpoint);
        log.debug("Registered endpoint: {} -> {}", id, path);
    }
    
    /**
     * Get market indices data
     * 
     * @return ApiResponse containing indices data
     */
    public ApiResponse getIndicesData() {
        return callEndpoint(TRADEBRAIN_ENDPOINT_INDEX);
    }
    
    /**
     * Get stock details data
     * 
     * @param symbol Stock symbol
     * @return ApiResponse containing stock details
     */
    public ApiResponse getStockDetails(String symbol) {
        return callEndpointWithSymbol(TRADEBRAIN_STOCK_ENDPOINT_DETAILS, symbol);
    }
    
    /**
     * Get stock historical data
     * 
     * @param symbol Stock symbol
     * @return ApiResponse containing historical data
     */
    public ApiResponse getStockHistorical(String symbol) {
        return callEndpointWithSymbol(TRADEBRAIN_STOCK_ENDPOINT_HISTORICAL, symbol);
    }
    
    /**
     * Get stock technical data
     * 
     * @param symbol Stock symbol
     * @return ApiResponse containing technical data
     */
    public ApiResponse getStockTechnical(String symbol) {
        return callEndpointWithSymbol(TRADEBRAIN_STOCK_ENDPOINT_TECHNICAL, symbol);
    }
    
    /**
     * Get stock shareholding data
     * 
     * @param symbol Stock symbol
     * @return ApiResponse containing shareholding data
     */
    public ApiResponse getStockShareholding(String symbol) {
        return callEndpointWithSymbol(TRADEBRAIN_STOCK_ENDPOINT_SHAREHOLDING, symbol);
    }
    
    /**
     * Get stock heatmap data
     * 
     * @param symbol Stock symbol
     * @return ApiResponse containing heatmap data
     */
    public ApiResponse getStockHeatmap(String symbol) {
        return callEndpointWithSymbol(TRADEBRAIN_STOCK_ENDPOINT_HEATMAP, symbol);
    }
    
    /**
     * Call an endpoint
     * 
     * @param endpointId Endpoint ID
     * @return ApiResponse from the endpoint
     */
    private ApiResponse callEndpoint(String endpointId) {
        ApiEndpoint endpoint = endpointRegistry.getEndpoint(endpointId);
        if (endpoint == null) {
            log.error("Endpoint not found: {}", endpointId);
            return null;
        }
        
        String url = endpoint.getUrl();
        return apiClient.get(url, endpoint.getHeaders());
    }
    
    /**
     * Call an endpoint with a symbol parameter
     * 
     * @param endpointId Endpoint ID
     * @param symbol Stock symbol
     * @return ApiResponse from the endpoint
     */
    private ApiResponse callEndpointWithSymbol(String endpointId, String symbol) {
        ApiEndpoint endpoint = endpointRegistry.getEndpoint(endpointId);
        if (endpoint == null) {
            log.error("Endpoint not found: {}", endpointId);
            return null;
        }
        
        String url = endpoint.getUrl().replace("{symbol}", symbol);
        return apiClient.get(url, endpoint.getHeaders());
    }
    
    /**
     * Check health of a specific endpoint
     * 
     * @param endpointId Endpoint ID
     * @return ApiResponse from the endpoint health check
     */
    public ApiResponse checkEndpointHealth(String endpointId) {
        return callEndpoint(endpointId);
    }
    
    /**
     * Check health of a specific endpoint with a symbol parameter
     * 
     * @param endpointId Endpoint ID
     * @param symbol Stock symbol to use
     * @return ApiResponse from the endpoint health check
     */
    public ApiResponse checkEndpointHealthWithSymbol(String endpointId, String symbol) {
        return callEndpointWithSymbol(endpointId, symbol);
    }
}
