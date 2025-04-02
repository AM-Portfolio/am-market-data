package com.am.marketdata.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.common.investment.model.equity.financial.balancesheet.StockBalanceSheet;
import com.am.common.investment.model.equity.financial.cashflow.StockCashFlow;
import com.am.common.investment.model.equity.financial.factsheetdividend.StockFactSheetDividend;
import com.am.common.investment.model.equity.financial.profitandloss.StockProfitAndLoss;
import com.am.common.investment.model.equity.financial.resultstatement.QuaterlyResult;
import com.am.common.investment.model.equity.financial.resultstatement.StockFinancialResult;
import com.am.marketdata.common.model.events.BalanceSheetFinancialsUpdateEvent;
import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEvent;
import com.am.marketdata.common.model.events.CashFlowFinancialsUpdateEvent;
import com.am.marketdata.common.model.events.FactSheetFinancialsUpdateEvent;
import com.am.marketdata.common.model.events.QuaterlyFinancialsUpdateEvent;
import com.am.marketdata.common.model.events.StockProfitAndLossFinancialsUpdateEvent;
import com.am.marketdata.common.model.events.StockResultsFinancialsUpdateEvent;

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
    private final KafkaTemplate<String, FactSheetFinancialsUpdateEvent> factSheetFinancialsKafkaTemplate;
    private final KafkaTemplate<String, CashFlowFinancialsUpdateEvent> cashFlowFinancialsKafkaTemplate;
    private final KafkaTemplate<String, BalanceSheetFinancialsUpdateEvent> balanceSheetFinancialsKafkaTemplate;
    private final KafkaTemplate<String, StockProfitAndLossFinancialsUpdateEvent> profitAndLossFinancialsKafkaTemplate;
    private final KafkaTemplate<String, StockResultsFinancialsUpdateEvent> stockResultsFinancialsKafkaTemplate;
    
    @Value("${app.kafka.board-of-directors-topic:stock-board-of-directors}")
    private String boardOfDirectorsTopic;
    
    @Value("${app.kafka.quaterly-financials-topic:stock-quaterly-financials}")
    private String quaterlyFinancialsTopic;
    
    @Value("${app.kafka.fact-sheet-financials-topic:stock-fact-sheet-financials}")
    private String factSheetFinancialsTopic;
    
    @Value("${app.kafka.cash-flow-financials-topic:stock-cash-flow-financials}")
    private String cashFlowFinancialsTopic;
    
    @Value("${app.kafka.balance-sheet-financials-topic:stock-balance-sheet-financials}")
    private String balanceSheetFinancialsTopic;
    
    @Value("${app.kafka.profit-and-loss-financials-topic:stock-profit-and-loss-financials}")
    private String profitAndLossFinancialsTopic;
    
    @Value("${app.kafka.stock-results-financials-topic:stock-results-financials}")
    private String stockResultsFinancialsTopic;
    
    public void sendBoardOfDirectorsUpdate(String symbol, BoardOfDirectors boardOfDirectors) {
        var event = BoardOfDirectorsUpdateEvent.builder()
            .eventType("BOARD_OF_DIRECTORS_UPDATE")
            .timestamp(LocalDateTime.now())
            .symbol(symbol)
            .boardOfDirector(boardOfDirectors)
            .build();
        
        sendBoardOfDirectorsUpdate(event, boardOfDirectorsTopic, event.getEventType(), event.getTimestamp());
    }

    public void sendQuaterlyFinancialsUpdate(String symbol, QuaterlyResult quaterlyResult) {
        var event = QuaterlyFinancialsUpdateEvent.builder()
            .eventType("QUATERLY_FINANCIALS_UPDATE")
            .timestamp(LocalDateTime.now())
            .symbol(symbol)
            .quaterlyResult(quaterlyResult)
            .build();
        
        sendQuaterlyFinancialsUpdate(event, quaterlyFinancialsTopic, event.getEventType(), event.getTimestamp());
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

    public void sendBalanceSheetFinancialsUpdate(String symbol, StockBalanceSheet balanceSheet) {
        var event = BalanceSheetFinancialsUpdateEvent.builder()
            .eventType("BALANCE_SHEET_FINANCIALS_UPDATE")
            .timestamp(LocalDateTime.now())
            .symbol(symbol)
            .balanceSheet(balanceSheet)
            .build();
        
        sendBalanceSheetFinancialsUpdate(event, balanceSheetFinancialsTopic, event.getEventType(), event.getTimestamp());
    }

    public void sendFactSheetFinancialsUpdate(String symbol, StockFactSheetDividend factSheetDividend) {
        var event = FactSheetFinancialsUpdateEvent.builder()
            .eventType("FACT_SHEET_FINANCIALS_UPDATE")
            .timestamp(LocalDateTime.now())
            .symbol(symbol)
            .factSheetDividend(factSheetDividend)
            .build();
        
        sendFactSheetFinancialsUpdate(event, factSheetFinancialsTopic, event.getEventType(), event.getTimestamp());
    }

    public void sendCashFlowFinancialsUpdate(String symbol, StockCashFlow cashFlow) {
        var event = CashFlowFinancialsUpdateEvent.builder()
            .eventType("CASH_FLOW_FINANCIALS_UPDATE")
            .timestamp(LocalDateTime.now())
            .symbol(symbol)
            .cashFlow(cashFlow)
            .build();
        
        sendCashFlowFinancialsUpdate(event, cashFlowFinancialsTopic, event.getEventType(), event.getTimestamp());
    }

    public void sendStockProfitAndLossFinancialsUpdate(String symbol, StockProfitAndLoss profitAndLoss) {
        var event = StockProfitAndLossFinancialsUpdateEvent.builder()
            .eventType("STOCK_PROFIT_AND_LOSS_FINANCIALS_UPDATE")
            .timestamp(LocalDateTime.now())
            .symbol(symbol)
            .profitAndLoss(profitAndLoss)
            .build();
        
        sendStockProfitAndLossFinancialsUpdate(event, profitAndLossFinancialsTopic, event.getEventType(), event.getTimestamp());
    }

    public void sendStockResultsFinancialsUpdate(String symbol, StockFinancialResult results) {
        var event = StockResultsFinancialsUpdateEvent.builder()
            .eventType("STOCK_RESULTS_FINANCIALS_UPDATE")
            .timestamp(LocalDateTime.now())
            .symbol(symbol)
            .financialsReport(results)
            .build();
        
        sendStockResultsFinancialsUpdate(event, stockResultsFinancialsTopic, event.getEventType(), event.getTimestamp());
    }

    private void sendFactSheetFinancialsUpdate(
            FactSheetFinancialsUpdateEvent event,
            String topic,
            String eventType,
            LocalDateTime timestamp) {
        
        try {
            factSheetFinancialsKafkaTemplate.send(topic, event);
            log.info("Fact sheet financials update event sent successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send fact sheet financials update event to Kafka", e);
            throw e;
        }
    }

    private void sendCashFlowFinancialsUpdate(
            CashFlowFinancialsUpdateEvent event,
            String topic,
            String eventType,
            LocalDateTime timestamp) {
        
        try {
            cashFlowFinancialsKafkaTemplate.send(topic, event);
            log.info("Cash flow financials update event sent successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send cash flow financials update event to Kafka", e);
            throw e;
        }
    }

    private void sendBalanceSheetFinancialsUpdate(
            BalanceSheetFinancialsUpdateEvent event,
            String topic,
            String eventType,
            LocalDateTime timestamp) {
        
        try {
            balanceSheetFinancialsKafkaTemplate.send(topic, event);
            log.info("Balance sheet financials update event sent successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send balance sheet financials update event to Kafka", e);
            throw e;
        }
    }

    private void sendStockProfitAndLossFinancialsUpdate(
            StockProfitAndLossFinancialsUpdateEvent event,
            String topic,
            String eventType,
            LocalDateTime timestamp) {
        
        try {
            profitAndLossFinancialsKafkaTemplate.send(topic, event);
            log.info("Stock profit and loss financials update event sent successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send stock profit and loss financials update event to Kafka", e);
            throw e;
        }
    }

    private void sendStockResultsFinancialsUpdate(
            StockResultsFinancialsUpdateEvent event,
            String topic,
            String eventType,
            LocalDateTime timestamp) {
        
        try {
            stockResultsFinancialsKafkaTemplate.send(topic, event);
            log.info("Stock results financials update event sent successfully to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send stock results financials update event to Kafka", e);
            throw e;
        }
    }
}
