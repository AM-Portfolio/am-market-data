package com.am.marketdata.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEvent;
import com.am.marketdata.kafka.config.KafkaConfig;

import lombok.RequiredArgsConstructor;

/**
 * Service for producing stock portfolio events to Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockPortfolioProducerService {
    
    private final KafkaTemplate<String, BoardOfDirectorsUpdateEvent> boardOfDirectorsKafkaTemplate;
    
    @Value("${app.kafka.board-of-directors-topic:stock-board-of-directors}")
    private String boardOfDirectorsTopic;
    
    public void sendBoardOfDirectorsUpdate(String symbol, BoardOfDirectors boardOfDirectors) {
        var event = BoardOfDirectorsUpdateEvent.builder()
            .eventType("BOARD_OF_DIRECTORS_UPDATE")
            .timestamp(LocalDateTime.now())
            .symbol(symbol)
            .boardOfDirector(boardOfDirectors)
            .build();
        
        sendBoardOfDirectorsUpdate(event, boardOfDirectorsTopic, event.getEventType(), event.getTimestamp());
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
