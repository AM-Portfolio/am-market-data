package com.am.marketdata.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.common.investment.model.equity.financial.balancesheet.StockBalanceSheet;
import com.am.common.investment.model.equity.financial.cashflow.StockCashFlow;
import com.am.common.investment.model.equity.financial.factsheetdividend.StockFactSheetDividend;
import com.am.common.investment.model.equity.financial.profitandloss.StockProfitAndLoss;
import com.am.common.investment.model.equity.financial.resultstatement.QuaterlyResult;
import com.am.common.investment.model.equity.financial.resultstatement.StockFinancialResult;
import com.am.marketdata.common.model.events.BalanceSheetFinancialsUpdateEventV1;
import com.am.marketdata.common.model.events.BoardOfDirectorsUpdateEventV1;
import com.am.marketdata.common.model.events.CashFlowFinancialsUpdateEventV1;
import com.am.marketdata.common.model.events.FactSheetFinancialsUpdateEventV1;
import com.am.marketdata.common.model.events.QuarterlyFinancialsUpdateEventV1;
import com.am.marketdata.common.model.events.StockProfitAndLossFinancialsUpdateEventV1;
import com.am.marketdata.common.model.events.StockResultsFinancialsUpdateEventV1;
import com.am.marketdata.kafka.config.KafkaProperties;

import lombok.RequiredArgsConstructor;

/**
 * Service for producing stock portfolio events to Kafka
 * Uses generic BaseKafkaProducer instances for each event type
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockPortfolioProducerService {

    private final BaseKafkaProducer<BoardOfDirectorsUpdateEventV1> boardOfDirectorsProducer;
    private final BaseKafkaProducer<QuarterlyFinancialsUpdateEventV1> quaterlyFinancialsProducer;
    private final BaseKafkaProducer<FactSheetFinancialsUpdateEventV1> factSheetFinancialsProducer;
    private final BaseKafkaProducer<CashFlowFinancialsUpdateEventV1> cashFlowFinancialsProducer;
    private final BaseKafkaProducer<BalanceSheetFinancialsUpdateEventV1> balanceSheetFinancialsProducer;
    private final BaseKafkaProducer<StockProfitAndLossFinancialsUpdateEventV1> profitAndLossFinancialsProducer;
    private final BaseKafkaProducer<StockResultsFinancialsUpdateEventV1> stockResultsFinancialsProducer;

    private final KafkaProperties kafkaProperties;

    public void sendBoardOfDirectorsUpdate(String symbol, BoardOfDirectors boardOfDirectors) {
        var event = BoardOfDirectorsUpdateEventV1.builder()
                .eventType("BOARD_OF_DIRECTORS_UPDATE")
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .boardOfDirector(boardOfDirectors)
                .build();

        try {
            boardOfDirectorsProducer.send(kafkaProperties.getTopics().getStockBoardOfDirectors(), event);
            log.info("Board of directors update event sent successfully to topic: {}",
                    kafkaProperties.getTopics().getStockBoardOfDirectors());
        } catch (Exception e) {
            log.error("Failed to send board of directors update event to Kafka", e);
            throw e;
        }
    }

    public void sendQuaterlyFinancialsUpdate(String symbol, QuaterlyResult quaterlyResult) {
        var event = QuarterlyFinancialsUpdateEventV1.builder()
                .eventType("QUATERLY_FINANCIALS_UPDATE")
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .quaterlyResult(quaterlyResult)
                .build();

        try {
            quaterlyFinancialsProducer.send(kafkaProperties.getTopics().getStockQuaterlyFinancials(), event);
            log.info("Quaterly financials update event sent successfully to topic: {}",
                    kafkaProperties.getTopics().getStockQuaterlyFinancials());
        } catch (Exception e) {
            log.error("Failed to send quaterly financials update event to Kafka", e);
            throw e;
        }
    }

    public void sendBalanceSheetFinancialsUpdate(String symbol, StockBalanceSheet balanceSheet) {
        var event = BalanceSheetFinancialsUpdateEventV1.builder()
                .eventType("BALANCE_SHEET_FINANCIALS_UPDATE")
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .balanceSheet(balanceSheet)
                .build();

        try {
            balanceSheetFinancialsProducer.send(kafkaProperties.getTopics().getStockBalanceSheetFinancials(), event);
            log.info("Balance sheet financials update event sent successfully to topic: {}",
                    kafkaProperties.getTopics().getStockBalanceSheetFinancials());
        } catch (Exception e) {
            log.error("Failed to send balance sheet financials update event to Kafka", e);
            throw e;
        }
    }

    public void sendFactSheetFinancialsUpdate(String symbol, StockFactSheetDividend factSheetDividend) {
        var event = FactSheetFinancialsUpdateEventV1.builder()
                .eventType("FACT_SHEET_FINANCIALS_UPDATE")
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .factSheetDividend(factSheetDividend)
                .build();

        try {
            factSheetFinancialsProducer.send(kafkaProperties.getTopics().getStockFactSheetDividendFinancials(), event);
            log.info("Fact sheet financials update event sent successfully to topic: {}",
                    kafkaProperties.getTopics().getStockFactSheetDividendFinancials());
        } catch (Exception e) {
            log.error("Failed to send fact sheet financials update event to Kafka", e);
            throw e;
        }
    }

    public void sendCashFlowFinancialsUpdate(String symbol, StockCashFlow cashFlow) {
        var event = CashFlowFinancialsUpdateEventV1.builder()
                .eventType("CASH_FLOW_FINANCIALS_UPDATE")
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .cashFlow(cashFlow)
                .build();

        try {
            cashFlowFinancialsProducer.send(kafkaProperties.getTopics().getStockCashFlowFinancials(), event);
            log.info("Cash flow financials update event sent successfully to topic: {}",
                    kafkaProperties.getTopics().getStockCashFlowFinancials());
        } catch (Exception e) {
            log.error("Failed to send cash flow financials update event to Kafka", e);
            throw e;
        }
    }

    public void sendStockProfitAndLossFinancialsUpdate(String symbol, StockProfitAndLoss profitAndLoss) {
        var event = StockProfitAndLossFinancialsUpdateEventV1.builder()
                .eventType("STOCK_PROFIT_AND_LOSS_FINANCIALS_UPDATE")
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .profitAndLoss(profitAndLoss)
                .build();

        try {
            profitAndLossFinancialsProducer.send(kafkaProperties.getTopics().getStockProfitAndLossFinancials(), event);
            log.info("Profit and loss financials update event sent successfully to topic: {}",
                    kafkaProperties.getTopics().getStockProfitAndLossFinancials());
        } catch (Exception e) {
            log.error("Failed to send profit and loss financials update event to Kafka", e);
            throw e;
        }
    }

    public void sendStockResultsFinancialsUpdate(String symbol, StockFinancialResult results) {
        var event = StockResultsFinancialsUpdateEventV1.builder()
                .eventType("STOCK_RESULTS_FINANCIALS_UPDATE")
                .timestamp(LocalDateTime.now())
                .symbol(symbol)
                .financialsReport(results)
                .build();

        try {
            stockResultsFinancialsProducer.send(kafkaProperties.getTopics().getStockResultsFinancials(), event);
            log.info("Stock results financials update event sent successfully to topic: {}",
                    kafkaProperties.getTopics().getStockResultsFinancials());
        } catch (Exception e) {
            log.error("Failed to send stock results financials update event to Kafka", e);
            throw e;
        }
    }
}
