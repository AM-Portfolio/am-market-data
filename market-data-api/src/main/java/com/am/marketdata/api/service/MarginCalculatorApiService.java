package com.am.marketdata.api.service;

import com.marketdata.common.model.margin.MarginCalculationRequest;
import com.marketdata.common.model.margin.MarginCalculationResponse;

/**
 * Interface for Margin Calculator API Service
 */
public interface MarginCalculatorApiService {
    /**
     * Calculate margin required for a trade
     * 
     * @param request Margin calculation request
     * @return Margin calculation response
     */
    MarginCalculationResponse calculateMargin(MarginCalculationRequest request);
}
