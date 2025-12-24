package com.am.marketdata.api.util;

import com.am.common.investment.model.stockindice.StockIndicesMarketData;
import com.am.common.investment.service.StockIndicesMarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentUtils {

    private final StockIndicesMarketDataService stockIndicesMarketDataService;

    /**
     * Resolves a comma-separated string of symbols (or indices) into a unique set
     * of individual stock symbols.
     * If a symbol is an index, all its constituent stocks are added.
     *
     * @param commaSeparatedSymbols String containing symbols separated by commas.
     * @return Set of unique stock symbols.
     */
    public Set<String> resolveSymbols(String commaSeparatedSymbols) {
        if (commaSeparatedSymbols == null || commaSeparatedSymbols.trim().isEmpty()) {
            return new HashSet<>();
        }
        List<String> rawSymbols = Arrays.stream(commaSeparatedSymbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        return resolveSymbols(rawSymbols);
    }

    /**
     * Resolves a list of symbols (or indices) into a unique set of individual stock
     * symbols.
     * If a symbol is an index, all its constituent stocks are added.
     *
     * @param rawSymbols List of symbols or indices.
     * @return Set of unique stock symbols.
     */
    public Set<String> resolveSymbols(List<String> rawSymbols) {
        Set<String> finalSymbols = new HashSet<>();

        for (String symbol : rawSymbols) {
            try {
                // Check if the symbol is an index
                StockIndicesMarketData indexData = stockIndicesMarketDataService.findByIndexSymbol(symbol);

                if (indexData != null && indexData.getData() != null) {
                    // It is an index, add all constituents
                    List<String> constituents = indexData.getData().stream()
                            .map(data -> data.getSymbol())
                            .collect(Collectors.toList());
                    finalSymbols.addAll(constituents);
                    log.debug("Resolved index {} to {} symbols", symbol, constituents.size());
                } else {
                    // Not an index or no data, treat as regular symbol
                    finalSymbols.add(symbol);
                }
            } catch (Exception e) {
                // On error (e.g. not found if service throws), assume it's a regular symbol
                log.warn("Error resolving symbol {}, treating as regular symbol: {}", symbol, e.getMessage());
                finalSymbols.add(symbol);
            }
        }
        return finalSymbols;
    }
}
