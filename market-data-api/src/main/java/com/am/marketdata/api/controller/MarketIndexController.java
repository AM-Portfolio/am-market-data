package com.am.marketdata.api.controller;

import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.common.investment.service.StockIndicesMarketDataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nse-indices")
public class MarketIndexController {
    
    @Autowired
    private StockIndicesMarketDataService marketIndexService;

    @GetMapping("/{indexSymbol}")
    public ResponseEntity<StockIndicesMarketData> getLatestIndexData(@PathVariable String indexSymbol) {
        return ResponseEntity.ok(marketIndexService.findByIndexSymbol(indexSymbol));
    }
}
