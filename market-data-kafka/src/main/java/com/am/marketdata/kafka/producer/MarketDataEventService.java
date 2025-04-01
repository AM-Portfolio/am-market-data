// package com.am.marketdata.kafka.producer;

// import com.am.common.investment.model.equity.EquityPrice;
// import com.am.common.investment.model.board.BoardOfDirectors;
// import com.am.common.investment.model.equity.EquityPrice.EquityPriceBuilder;
// import com.am.common.investment.model.equity.MarketIndexIndices;
// import com.am.common.investment.model.equity.financial.resultstatement.QuaterlyResult;
// import com.am.common.investment.model.events.EquityPriceUpdateEvent;
// import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
// import com.am.common.investment.model.events.StockIndicesPriceUpdateEvent;
// import com.am.common.investment.model.events.StockInsidicesEventData;
// import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEvent;
// import com.am.marketdata.common.model.events.QuaterlyFinancialsUpdateEvent;
// import com.am.marketdata.kafka.config.KafkaTopicsConfig;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.concurrent.CompletableFuture;

// /**
//  * Unified service for all market data event publishing
//  * Designed to handle 50+ event types with minimal code duplication
//  */
// @Slf4j
// @Service
// public class MarketDataEventService {

//     private final GenericEventPublisher publisher;
//     private final KafkaTopicsConfig topicsConfig;

//     public MarketDataEventService(GenericEventPublisher publisher, KafkaTopicsConfig topicsConfig) {
//         this.publisher = publisher;
//         this.topicsConfig = topicsConfig;
//     }

//     /**
//      * Publish equity price updates
//      */
//     public boolean publishEquityPriceUpdates(List<EquityPrice> equityPrices) {
//         try {
//             log.info("Publishing equity price updates for {} equities", equityPrices.size());
            
//             var event = EquityPriceUpdateEvent.builder()
//                     .eventType("EQUITY_PRICE_UPDATE")
//                     .timestamp(LocalDateTime.now())
//                     .equityPrices(equityPrices)
//                     .build();
            
//             return publisher.publishEvent(event, topicsConfig.getStockPriceTopic(), event.getEventType(), event.getTimestamp());
//         } catch (Exception e) {
//             log.error("Failed to publish equity price updates", e);
//             return false;
//         }
//     }

//     /**
//      * Publish stock indices update
//      */
//     public boolean publishStockIndicesUpdate(StockInsidicesEventData stockIndices) {
//         try {
//             log.info("Publishing stock indices update for {}", stockIndices.getName());
            
//             var event = StockIndicesPriceUpdateEvent.builder()
//                     .eventType("STOCK_INDICES_UPDATE")
//                     .timestamp(LocalDateTime.now())
//                     .stockIndices(stockIndices)
//                     .build();
            
//             return publisher.publishEvent(event, topicsConfig.getStockIndicesTopic(), event.getEventType(), event.getTimestamp());
//         } catch (Exception e) {
//             log.error("Failed to publish stock indices update", e);
//             return false;
//         }
//     }

//     /**
//      * Publish market indices update
//      */
//     public boolean publishMarketIndicesUpdate(List<MarketIndexIndices> marketIndexIndices) {
//         try {
//             log.info("Publishing market indices update for {} indices", marketIndexIndices.size());
            
//             var event = MarketIndexIndicesPriceUpdateEvent.builder()
//                     .eventType("MARKET_INDICES_UPDATE")
//                     .timestamp(LocalDateTime.now())
//                     .marketIndices(marketIndexIndices)
//                     .build();
            
//             return publisher.publishEvent(event, topicsConfig.getNseIndicesTopic(), event.getEventType(), event.getTimestamp());
//         } catch (Exception e) {
//             log.error("Failed to publish market indices update", e);
//             return false;
//         }
//     }

//     /**
//      * Publish board of directors update
//      */
//     public boolean publishBoardOfDirectorsUpdate(String symbol, BoardOfDirectors boardOfDirectors) {
//         try {
//             log.info("Publishing board of directors update for symbol: {}", symbol);
            
//             var event = BoardOfDirectorsUpdateEvent.builder()
//                     .eventType("BOARD_OF_DIRECTORS_UPDATE")
//                     .timestamp(LocalDateTime.now())
//                     .symbol(symbol)
//                     .boardOfDirector(boardOfDirectors)
//                     .build();
            
//             return publisher.publishEvent(event, topicsConfig.getBoardOfDirectorsTopic(), event.getEventType(), event.getTimestamp());
//         } catch (Exception e) {
//             log.error("Failed to publish board of directors update", e);
//             return false;
//         }
//     }

//     /**
//      * Publish quarterly financials update
//      */
//     public boolean publishQuaterlyFinancialsUpdate(String symbol, QuaterlyResult quaterlyResult) {
//         try {
//             log.info("Publishing quarterly financials update for symbol: {}", symbol);
            
//             var event = QuaterlyFinancialsUpdateEvent.builder()
//                     .eventType("QUATERLY_FINANCIALS_UPDATE")
//                     .timestamp(LocalDateTime.now())
//                     .symbol(symbol)
//                     .quaterlyResult(quaterlyResult)
//                     .build();
            
//             return publisher.publishEvent(event, topicsConfig.getQuaterlyFinancialsTopic(), event.getEventType(), event.getTimestamp());
//         } catch (Exception e) {
//             log.error("Failed to publish quarterly financials update", e);
//             return false;
//         }
//     }
    
//     /**
//      * Publish multiple events in parallel with resilient error handling
//      * Implements the partial success strategy pattern used in your market data processing
//      */
//     public void publishMultipleEvents(Runnable... publishers) {
//         CompletableFuture<?>[] futures = new CompletableFuture[publishers.length];
        
//         for (int i = 0; i < publishers.length; i++) {
//             final int index = i;
//             futures[i] = CompletableFuture.runAsync(() -> {
//                 try {
//                     publishers[index].run();
//                 } catch (Exception e) {
//                     log.error("Failed to publish event {}: {}", index, e.getMessage());
//                     // Swallow exception to allow other publishers to continue
//                 }
//             });
//         }
        
//         // Wait for all futures to complete
//         CompletableFuture.allOf(futures).join();
//     }
// }
