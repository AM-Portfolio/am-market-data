package com.am.marketdata.service.impl;

import com.am.marketdata.api.service.MarginCalculatorApiService;
import com.marketdata.common.model.margin.MarginCalculationRequest;
import com.marketdata.common.model.margin.MarginCalculationResponse;
import com.am.marketdata.service.margin.MarginCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of MarginCalculatorApiService that delegates to
 * MarginCalculatorService
 */
@Service
@RequiredArgsConstructor
public class MarginCalculatorApiServiceImpl implements MarginCalculatorApiService {

    private final MarginCalculatorService marginCalculatorService;

    @Override
    public MarginCalculationResponse calculateMargin(MarginCalculationRequest request) {
        return marginCalculatorService.calculateMargin(request);
    }
}
