package com.am.marketdata.api.service;

import com.am.marketdata.common.model.SecurityDTOV1;
import com.am.marketdata.common.model.SecuritySearchRequestV1;

import java.util.List;
import java.util.Map;

/**
 * Interface for Security API Service (Contract only)
 */
public interface SecurityApiService {
    /**
     * Find securities by symbols
     */
    List<SecurityDTOV1> findBySymbols(List<String> symbols);

    /**
     * Get symbol to sector mapping
     */
    Map<String, String> getSymbolToSectorMap(List<String> symbols);

    /**
     * Search for securities based on criteria
     */
    List<SecurityDTOV1> search(SecuritySearchRequestV1 request);

    /**
     * Get all securities
     */
    List<SecurityDTOV1> getAllSecurities();
}
