package com.am.marketdata.kafka.oldProducer;

import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.model.equity.financial.resultstatement.QuaterlyResult;
import com.am.common.investment.model.events.EquityPriceUpdateEvent;
import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.StockIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.StockInsidicesEventData;
import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEvent;
import com.am.marketdata.common.model.events.QuaterlyFinancialsUpdateEvent;
import com.am.marketdata.kafka.config.KafkaTopicsConfig;
import com.am.marketdata.kafka.oldProducer.impl.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Unified service for publishing all market data events to Kafka
 */
@Slf4j
@Service
public class MarketDataEventPublisher {

    private final KafkaTopicsConfig topicsConfig;
    private final EquityPriceEventProducer equityPriceProducer;
    private final StockIndicesEventProducer stockIndicesProducer;
    private final MarketIndicesEventProducer marketIndicesProducer;
    private final BoardOfDirectorsEventProducer boardOfDirectorsProducer;
    private final QuaterlyFinancialsEventProducer quaterlyFinancialsProducer;

    public MarketDataEventPublisher(
            KafkaTopicsConfig topicsConfig,
            EquityPriceEventProducer equityPriceProducer,
            StockIndicesEventProducer stockIndicesProducer,
            MarketIndicesEventProducer marketIndicesProducer,
            BoardOfDirectorsEventProducer boardOfDirectorsProducer,
            QuaterlyFinancialsEventProducer quaterlyFinancialsProducer) {
        this.topicsConfig = topicsConfig;
        this.equityPriceProducer = equityPriceProducer;
        this.stockIndicesProducer = stockIndicesProducer;
        this.marketIndicesProducer = marketIndicesProducer;
        this.boardOfDirectorsProducer = boardOfDirectorsProducer;
        this.quaterlyFinancialsProducer = quaterlyFinancialsProducer;
    }

    /**
     * Publish equity price updates
     */
    public void publishEquityPriceUpdates(List<EquityPrice> equityPrices) {
        log.info("Publishing equity price updates for {} equities", equityPrices.size());
        
        var event = EquityPriceUpdateEvent.builder()
                .eventType("EQUITY_PRICE_UPDATE")
                .timestamp(LocalDateTime.now())
                .equityPrices(equityPrices)
                .build();
        
        equityPriceProducer.sendEvent(
                event, 
                topicsConfig.getStockPriceTopic(), 
                event.getEventType(), 
                event.getTimestamp());
    }

    /**
     * Publish stock indices updates
     */
    public void publishStockIndicesUpdate(StockInsidicesEventData stockIndices) {
        log.info("Publishing stock indices update for {}", stockIndices.getName());
        
        var event = StockIndicesPriceUpdateEvent.builder()
                .eventType("STOCK_INDICES_UPDATE")
                .timestamp(LocalDateTime.now())
                .stockIndices(stockIndices)
                .build();
        
        stockIndicesProducer.sendEvent(
                event, 
                topicsConfig.getStockIndicesTopic(), 
                event.getEventType(), 
                event.getTimestamp());
    }

    /**
     * Publish market indices updates
     */
    public void publishMarketIndicesUpdate(List<MarketIndexIndices> marketIndexIndices) {
        log.info("Publishing market indices update for {} indices", marketIndexIndices.size());
        
        var event = MarketIndexIndicesPriceUpdateEvent.builder()
                .eventType("MARKET_INDICES_UPDATE")
                .timestamp(LocalDateTime.now())
                .marketIndices(marketIndexIndices)
                .build();
        
        marketIndicesProducer.sendEvent(
                event, 
                topicsConfig.getNseIndicesTopic(), 
                event.getEventType(), 
                event.getTimestamp());
    }

    /**
     * Publish board of directors update
     */
    public void publishBoardOfDirectorsUpdate(String symbol, BoardOfDirectors boardOfDirectors) {
        log.info("Publishing board of directors update for symbol: {}", symbol);
        
        var event = BoardOfDirectorsUpdateEvent.builder()
                .eventType("BOARD_OF_DIRECTORS_UPDATE")
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .boardOfDirector(boardOfDirectors)
                .build();
        
        boardOfDirectorsProducer.sendEvent(
                event, 
                topicsConfig.getBoardOfDirectorsTopic(), 
                event.getEventType(), 
                event.getTimestamp());
    }

    /**
     * Publish quarterly financials update
     */
    public void publishQuaterlyFinancialsUpdate(String symbol, QuaterlyResult quaterlyResult) {
        log.info("Publishing quarterly financials update for symbol: {}", symbol);
        
        var event = QuaterlyFinancialsUpdateEvent.builder()
                .eventType("QUATERLY_FINANCIALS_UPDATE")
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .quaterlyResult(quaterlyResult)
                .build();
        
        quaterlyFinancialsProducer.sendEvent(
                event, 
                topicsConfig.getQuaterlyFinancialsTopic(), 
                event.getEventType(), 
                event.getTimestamp());
    }
}
