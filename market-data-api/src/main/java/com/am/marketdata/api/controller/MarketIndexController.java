// package com.am.marketdata.api.controller;

// import com.am.common.investment.model.stockindice.StockIndicesMarketData;
// import com.am.marketdata.api.service.StockIndicesService;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.Operation;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/v1/nse-indices")
// @RequiredArgsConstructor
// public class MarketIndexController {
//     private static final Logger log = LoggerFactory.getLogger(MarketIndexController.class);
//     private final StockIndicesService stockIndicesService;

//     @GetMapping("/{indexSymbol}")
//     @Operation(summary = "Get latest market data for a single NSE index")
//     public ResponseEntity<StockIndicesMarketData> getLatestIndexData(
//             @Parameter(description = "NSE Index symbol", required = true)
//             @PathVariable("indexSymbol") String indexSymbol,
//             @Parameter(description = "Force refresh from source instead of using cache")
//             @RequestParam(value = "forceRefresh", required = false, defaultValue = "false") boolean forceRefresh
//     ) {
//         StockIndicesMarketData data = stockIndicesService.getLatestIndexData(indexSymbol, forceRefresh);
        
//         return ResponseEntity.ok(data);
//     }

//     @PostMapping("/batch")
//     @Operation(summary = "Get latest market data for multiple NSE indices")
//     public ResponseEntity<List<StockIndicesMarketData>> getLatestIndicesData(
//             @Parameter(description = "List of NSE Index symbols", required = true)
//             @RequestBody List<String> indexSymbols,
//             @Parameter(description = "Force refresh from source instead of using cache")
//             @RequestParam(value = "forceRefresh", required = false, defaultValue = "false") boolean forceRefresh) {
        
//         List<StockIndicesMarketData> data = stockIndicesService.getLatestIndicesData(indexSymbols, forceRefresh);
        
//         return ResponseEntity.ok(data);
//     }   
// }
