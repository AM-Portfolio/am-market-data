package com.am.marketdata.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.am.common.investment.model.equity.ETFIndies;
import com.am.common.investment.model.equity.EquityPrice;
import com.am.common.investment.model.equity.MarketIndexIndices;
import com.am.common.investment.model.events.ETFIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.MarketIndexIndicesPriceUpdateEvent;
import com.am.marketdata.common.model.equity.StockInsidicesData;
import com.am.marketdata.kafka.model.StockIndicesPriceUpdateEvent;
import com.am.common.investment.model.events.EquityPriceUpdateEvent;

@Slf4j
@Service
public class KafkaProducerService {

    private final BaseKafkaProducer<EquityPriceUpdateEvent> equityProducer;
    private final BaseKafkaProducer<ETFIndicesPriceUpdateEvent> etfProducer;
    private final BaseKafkaProducer<MarketIndexIndicesPriceUpdateEvent> indicesProducer;
    private final BaseKafkaProducer<StockIndicesPriceUpdateEvent> stockIndicesProducer;

    @Value("${app.kafka.stock-price-topic}")
    private String stockPriceTopic;

    @Value("${app.kafka.nse-etf-topic}")
    private String nseEtfTopic;

    @Value("${app.kafka.nse-indices-topic}")
    private String nseIndicesTopic;
    
    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.equityProducer = new BaseKafkaProducer<>(kafkaTemplate) {};
        this.etfProducer = new BaseKafkaProducer<>(kafkaTemplate) {};
        this.indicesProducer = new BaseKafkaProducer<>(kafkaTemplate) {};
        this.stockIndicesProducer = new BaseKafkaProducer<>(kafkaTemplate) {};
    }

    public void sendEquityPriceUpdates(List<EquityPrice> equityPrices) {
        var event = EquityPriceUpdateEvent.builder()
            .eventType("EQUITY_PRICE_UPDATE")
            .timestamp(LocalDateTime.now())
            .equityPrices(equityPrices)
            .build();
        
        equityProducer.sendEvent(event, stockPriceTopic, event.getEventType(), event.getTimestamp());
    }

    public void sendETFUpdate(List<ETFIndies> etfIndies) {
        var event = ETFIndicesPriceUpdateEvent.builder()
            .eventType("ETF_PRICE_UPDATE")
            .timestamp(etfIndies.get(0).getTimestamp())
            .etfIndies(etfIndies)
            .build();
        
        etfProducer.sendEvent(event, nseEtfTopic, event.getEventType(), event.getTimestamp());
    }

    public void sendIndicesUpdate(List<MarketIndexIndices> marketIndexIndices) {
        var event = MarketIndexIndicesPriceUpdateEvent.builder()
            .eventType("INDICES_PRICE_UPDATE")  
            .timestamp(LocalDateTime.now())
            .marketIndices(marketIndexIndices)
            .build();
        
        indicesProducer.sendEvent(event, nseIndicesTopic, event.getEventType(), event.getTimestamp());
    }

    public void sendStockIndicesUpdate(StockInsidicesData stockIndices) {
        var event = StockIndicesPriceUpdateEvent.builder()
            .eventType("STOCK_INDICES_PRICE_UPDATE")  
            .timestamp(LocalDateTime.now())
            .stockIndices(stockIndices)
            .build();
        
        stockIndicesProducer.sendEvent(event, nseIndicesTopic, event.getEventType(), event.getTimestamp());
    }
}
