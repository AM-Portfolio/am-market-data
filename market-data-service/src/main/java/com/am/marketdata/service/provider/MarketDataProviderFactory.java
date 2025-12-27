package com.am.marketdata.service.provider;

import com.am.marketdata.provider.AMMarketDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for managing and retrieving market data providers
 */
@Slf4j
@Component
public class MarketDataProviderFactory {

    private final Map<String, AMMarketDataProvider> providers;

    @Autowired
    public MarketDataProviderFactory(List<AMMarketDataProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(
                        provider -> provider.getClass().getSimpleName().replace("MarketDataProvider", "").toLowerCase(),
                        Function.identity()));
        log.info("Initialized MarketDataProviderFactory with {} providers: {}",
                providers.size(), providers.keySet());
    }

    /**
     * Get a provider by name
     * 
     * @param providerName The provider name (e.g., "upstox", "zerodha")
     * @return The provider instance
     */
    public AMMarketDataProvider getProvider(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty");
        }

        String normalizedName = providerName.toLowerCase().trim();
        AMMarketDataProvider provider = providers.get(normalizedName);

        if (provider == null) {
            throw new IllegalArgumentException(
                    String.format("Provider '%s' not found. Available providers: %s",
                            providerName, providers.keySet()));
        }

        return provider;
    }

    /**
     * Get all available provider names
     * 
     * @return Set of provider names
     */
    public java.util.Set<String> getAvailableProviders() {
        return providers.keySet();
    }
}
