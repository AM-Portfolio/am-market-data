package com.am.marketdata.service.impl;

import com.am.marketdata.api.service.BrokerageCalculatorApiService;
import com.marketdata.common.model.margin.BrokerageCalculationRequest;
import com.marketdata.common.model.margin.BrokerageCalculationResponse;
import com.am.marketdata.service.margin.BrokerageCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of BrokerageCalculatorApiService that delegates to
 * BrokerageCalculatorService
 */
@Service
@RequiredArgsConstructor
public class BrokerageCalculatorApiServiceImpl implements BrokerageCalculatorApiService {

    private final BrokerageCalculatorService brokerageCalculatorService;

    @Override
    public BrokerageCalculationResponse calculateBrokerage(BrokerageCalculationRequest request) {
        return brokerageCalculatorService.calculateBrokerage(request);
    }
}
