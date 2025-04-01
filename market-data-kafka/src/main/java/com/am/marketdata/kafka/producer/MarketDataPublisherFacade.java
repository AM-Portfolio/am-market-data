package com.am.marketdata.kafka.producer;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.model.equity.financial.resultstatement.QuaterlyResult;
import com.am.common.investment.model.events.StockInsidicesEventData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Facade for market data publishing that provides a simple API
 * while leveraging the unified event publishing infrastructure.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataPublisherFacade {

    private final UnifiedEventPublisher publisher;

    /**
     * Publish equity price updates
     */
    public boolean publishEquityPriceUpdates(List<EquityPrice> equityPrices) {
        log.info("Publishing equity price updates for {} equities", equityPrices.size());
        return publisher.publish(EventTypeNames.EQUITY_PRICE_UPDATE, builder -> {
            try {
                builder.getClass().getMethod("equityPrices", List.class).invoke(builder, equityPrices);
            } catch (NoSuchMethodException e) {
                log.error("Method 'equityPrices' not found on builder class: {}", builder.getClass().getName(), e);
                throw new RuntimeException("Failed to invoke builder method: equityPrices", e);
            } catch (Exception e) {
                log.error("Failed to invoke 'equityPrices' method: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to invoke builder method: equityPrices", e);
            }
        });
    }

    /**
     * Publish stock indices update
     */
    public boolean publishStockIndicesUpdate(StockInsidicesEventData stockIndices) {
        log.info("Publishing stock indices update for {}", stockIndices.getName());
        return publisher.publish(EventTypeNames.STOCK_INDICES_UPDATE, builder -> {
            try {
                builder.getClass().getMethod("stockIndices", StockInsidicesEventData.class).invoke(builder, stockIndices);
            } catch (NoSuchMethodException e) {
                log.error("Method 'stockIndices' not found on builder class: {}", builder.getClass().getName(), e);
                throw new RuntimeException("Failed to invoke builder method: stockIndices", e);
            } catch (Exception e) {
                log.error("Failed to invoke 'stockIndices' method: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to invoke builder method: stockIndices", e);
            }
        });
    }

    /**
     * Publish market indices update
     */
    public boolean publishMarketIndicesUpdate(List<MarketIndexIndices> marketIndexIndices) {
        log.info("Publishing market indices update for {} indices", marketIndexIndices.size());
        return publisher.publish(EventTypeNames.MARKET_INDICES_UPDATE, builder -> {
            try {
                builder.getClass().getMethod("marketIndices", List.class).invoke(builder, marketIndexIndices);
            } catch (NoSuchMethodException e) {
                log.error("Method 'marketIndices' not found on builder class: {}", builder.getClass().getName(), e);
                throw new RuntimeException("Failed to invoke builder method: marketIndices", e);
            } catch (Exception e) {
                log.error("Failed to invoke 'marketIndices' method: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to invoke builder method: marketIndices", e);
            }
        });
    }

    /**
     * Publish board of directors update
     */
    public boolean publishBoardOfDirectorsUpdate(String symbol, BoardOfDirectors boardOfDirectors) {
        log.info("Publishing board of directors update for symbol: {}", symbol);
        return publisher.publish(EventTypeNames.BOARD_OF_DIRECTORS_UPDATE, builder -> {
            try {
                builder.getClass().getMethod("symbol", String.class).invoke(builder, symbol);
                builder.getClass().getMethod("boardOfDirector", BoardOfDirectors.class).invoke(builder, boardOfDirectors);
            } catch (NoSuchMethodException e) {
                log.error("Method 'symbol' or 'boardOfDirector' not found on builder class: {}", builder.getClass().getName(), e);
                throw new RuntimeException("Failed to invoke builder method: symbol or boardOfDirector", e);
            } catch (Exception e) {
                log.error("Failed to invoke 'symbol' or 'boardOfDirector' method: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to invoke builder method: symbol or boardOfDirector", e);
            }
        });
    }

    /**
     * Publish quarterly financials update
     */
    public boolean publishQuaterlyFinancialsUpdate(String symbol, QuaterlyResult quaterlyResult) {
        log.info("Publishing quarterly financials update for symbol: {}", symbol);
        return publisher.publish(EventTypeNames.QUATERLY_FINANCIALS_UPDATE, builder -> {
            try {
                builder.getClass().getMethod("symbol", String.class).invoke(builder, symbol);
                builder.getClass().getMethod("quaterlyResult", QuaterlyResult.class).invoke(builder, quaterlyResult);
            } catch (NoSuchMethodException e) {
                log.error("Method 'symbol' or 'quaterlyResult' not found on builder class: {}", builder.getClass().getName(), e);
                throw new RuntimeException("Failed to invoke builder method: symbol or quaterlyResult", e);
            } catch (Exception e) {
                log.error("Failed to invoke 'symbol' or 'quaterlyResult' method: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to invoke builder method: symbol or quaterlyResult", e);
            }
        });
    }
    
    /**
     * Publish multiple events in parallel with resilient error handling
     * Implements the partial success strategy pattern from your market data processing
     */
    public void publishMultipleAsync(Runnable... publishers) {
        publisher.publishMultipleAsync(Arrays.asList(publishers));
    }
    
    /**
     * Publish multiple events asynchronously with CompletableFuture
     * This method aligns with your existing resilient error handling patterns
     */
    public void publishAllAsync(List<CompletableFuture<?>> futures) {
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    log.error("Error in parallel event publishing: {}", ex.getMessage());
                    return null; // Continue with partial success
                })
                .join();
        } catch (Exception e) {
            log.error("Failed to publish events: {}", e.getMessage());
        }
    }
}
