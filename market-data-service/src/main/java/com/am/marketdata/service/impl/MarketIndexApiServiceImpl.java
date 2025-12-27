package com.am.marketdata.service.impl;

import com.am.marketdata.api.service.MarketIndexApiService;
import com.am.marketdata.common.model.NSEIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of MarketIndexApiService
 * Note: This is a placeholder implementation. Full implementation requires
 * integration with the scraper module or a dedicated index data service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketIndexApiServiceImpl implements MarketIndexApiService {

    @Override
    public List<NSEIndex> getAllIndices() {
        log.warn("MarketIndexApiService.getAllIndices() is not fully implemented yet");
        // TODO: Integrate with scraper module or create a dedicated index data
        // repository
        return Collections.emptyList();
    }

    @Override
    public NSEIndex getIndex(String symbol) {
        log.warn("MarketIndexApiService.getIndex({}) is not fully implemented yet", symbol);
        // TODO: Integrate with scraper module or create a dedicated index data
        // repository
        return null;
    }
}
