package com.am.marketdata.api.service;

import com.marketdata.common.model.margin.BrokerageCalculationRequest;
import com.marketdata.common.model.margin.BrokerageCalculationResponse;

/**
 * Interface for Brokerage Calculator API Service
 */
public interface BrokerageCalculatorApiService {
    /**
     * Calculate brokerage and taxes for a trade
     * 
     * @param request Brokerage calculation request
     * @return Brokerage calculation response
     */
    BrokerageCalculationResponse calculateBrokerage(BrokerageCalculationRequest request);
}
