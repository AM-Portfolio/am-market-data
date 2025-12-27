package com.am.marketdata.api.service;

import com.am.marketdata.common.model.SecurityDTO;
import com.am.marketdata.common.model.SecuritySearchRequest;

import java.util.List;
import java.util.Map;

/**
 * Interface for Security API Service (Contract only)
 */
public interface SecurityApiService {
    /**
     * Find securities by symbols
     */
    List<SecurityDTO> findBySymbols(List<String> symbols);

    /**
     * Get symbol to sector mapping
     */
    Map<String, String> getSymbolToSectorMap(List<String> symbols);

    /**
     * Search for securities based on criteria
     */
    List<SecurityDTO> search(SecuritySearchRequest request);

    /**
     * Get all securities
     */
    List<SecurityDTO> getAllSecurities();
}
