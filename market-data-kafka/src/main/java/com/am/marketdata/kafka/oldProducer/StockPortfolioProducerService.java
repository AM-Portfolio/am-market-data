package com.am.marketdata.kafka.oldProducer;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.common.investment.model.equity.financial.resultstatement.QuaterlyResult;
import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEvent;
import com.am.marketdata.common.model.events.QuaterlyFinancialsUpdateEvent;
import com.am.marketdata.kafka.config.KafkaTopicsConfig;

import lombok.RequiredArgsConstructor;

/**
 * Service for producing stock portfolio events to Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockPortfolioProducerService {
    
    private final KafkaTemplate<String, BoardOfDirectorsUpdateEvent> boardOfDirectorsKafkaTemplate;
    private final KafkaTemplate<String, QuaterlyFinancialsUpdateEvent> quaterlyFinancialsKafkaTemplate;
    private final KafkaTopicsConfig topicsConfig;
    
    public void sendBoardOfDirectorsUpdate(String symbol, BoardOfDirectors boardOfDirectors) {
        var event = BoardOfDirectorsUpdateEvent.builder()
            .eventType("BOARD_OF_DIRECTORS_UPDATE")
            .timestamp(LocalDateTime.now())
            .symbol(symbol)
            .boardOfDirector(boardOfDirectors)
            .build();
        
        sendBoardOfDirectorsUpdate(event, topicsConfig.getBoardOfDirectorsTopic(), event.getEventType(), event.getTimestamp());
    }

    public void sendQuaterlyFinancialsUpdate(String symbol, QuaterlyResult quaterlyResult) {
        var event = QuaterlyFinancialsUpdateEvent.builder()
            .eventType("QUATERLY_FINANCIALS_UPDATE")
            .timestamp(LocalDateTime.now())
            .symbol(symbol)
            .quaterlyResult(quaterlyResult)
            .build();
        
        sendQuaterlyFinancialsUpdate(event, topicsConfig.getQuaterlyFinancialsTopic(), event.getEventType(), event.getTimestamp());
    }

    private void sendQuaterlyFinancialsUpdate(
            QuaterlyFinancialsUpdateEvent event,
            String topic,
            String eventType,
            LocalDateTime timestamp) {
        
        try {
            quaterlyFinancialsKafkaTemplate.send(topic, event);
            log.info("Quaterly financials update event sent successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send quaterly financials update event to Kafka", e);
            throw e;
        }
    }

    private void sendBoardOfDirectorsUpdate(
            BoardOfDirectorsUpdateEvent event,
            String topic,
            String eventType,
            LocalDateTime timestamp) {
        
        try {
            boardOfDirectorsKafkaTemplate.send(topic, event);
            log.info("Board of directors update event sent successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send board of directors update event to Kafka", e);
            throw e;
        }
    }
}
