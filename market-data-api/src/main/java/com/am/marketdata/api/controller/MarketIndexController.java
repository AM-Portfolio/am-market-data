package com.am.marketdata.api.controller;

import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.service.MarketIndexIndicesService;
import com.am.marketdata.api.model.MarketIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market-index")
public class MarketIndexController {
    
    @Autowired
    private MarketIndexIndicesService marketIndexService;

    @GetMapping("/")
    public ResponseEntity<List<MarketIndexIndices>> getLatestIndexData() {
        return ResponseEntity.ok(marketIndexService.getByKey("BROAD MARKET INDICES"));
    }
}
