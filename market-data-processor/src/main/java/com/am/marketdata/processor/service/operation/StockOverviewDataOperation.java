package com.am.marketdata.processor.service.operation;

import com.am.common.investment.model.board.BoardOfDirectors;
import com.am.marketdata.common.model.events.BoardOfDirector;
import com.am.marketdata.external.api.client.TradeBrainClient;
import com.am.marketdata.external.api.model.ApiResponse;
import com.am.marketdata.processor.service.common.AbstractMarketDataOperation;
import com.am.marketdata.processor.service.common.DataFetcher;
import com.am.marketdata.processor.service.common.DataProcessor;
import com.am.marketdata.processor.service.common.DataValidator;
import com.am.marketdata.processor.service.mapper.StockBoardOfDirectorsMapper;
import com.am.marketdata.scraper.exception.DataFetchException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Operation for fetching and processing NSE stock indices data
 */
@Slf4j
@Component
public class StockOverviewDataOperation extends AbstractMarketDataOperation<BoardOfDirectors, Void> {
    
    private final TradeBrainClient tradeBrainClient;
    
    @Qualifier("stockBoardOfDirectoeProcessingTimer")
    private final Timer fetchTimer;
    private BoardOfDirectors lastFetchedData;
    private final StockBoardOfDirectorsMapper boardOfDirectorsMapper;
    
    @Value("${market.data.max.retries:3}")
    private int maxRetries;
    
    @Value("${market.data.retry.delay.ms:1000}")
    private long retryDelayMs;
    
    public StockOverviewDataOperation(
            DataFetcher dataFetcher,
            DataValidator<BoardOfDirectors> stockBoardOfDirectoeValidator,
            DataProcessor<BoardOfDirectors, Void> stockBoardOfDirectoeProcessor,
            MeterRegistry meterRegistry,
            Timer processingTimer,
            Executor executor,
            TradeBrainClient tradeBrainClient,
            StockBoardOfDirectorsMapper boardOfDirectorsMapper) {
        super(dataFetcher, stockBoardOfDirectoeValidator, stockBoardOfDirectoeProcessor, meterRegistry, processingTimer, executor);
        this.tradeBrainClient = tradeBrainClient;
        this.boardOfDirectorsMapper = boardOfDirectorsMapper;
        this.fetchTimer = processingTimer;
    }

    public StockOverviewDataOperation withSymbol(String symbol) {
        return (StockOverviewDataOperation) super.withIndexSymbol(symbol);
    }
    
    @Override
    protected String getDataTypeName() {
        return "stock-board-of-directors";
    }
    
    @Override
    protected Timer getFetchTimer() {
        return fetchTimer;
    }
    
    @Override
    protected void handleSuccess(Void result) {
        log.info("Successfully processed stock board of directors");
    }
    
    @Override
    @SneakyThrows
    protected BoardOfDirectors fetchData() {
        try {
            ApiResponse response = tradeBrainClient.getBoardOfDirectors(getIndexSymbol());
            List<BoardOfDirector> directors = boardOfDirectorsMapper.parseDirectors(response.getData());
            if (directors == null) {
                return null;
            }
            lastFetchedData = boardOfDirectorsMapper.toBoardOfDirectors(getIndexSymbol(), directors);
            return lastFetchedData;
        } catch (Exception e) {
            throw new DataFetchException(getDataTypeName(), maxRetries, "Failed to fetch stock board of directors data", e);
        }
    }
    
    /**
     * Get the current index symbol
     * 
     * @return The index symbol
     */
    public String getIndexSymbol() {
        return super.getIndexSymbol();
    }

    /**
     * Get the last fetched data from the API
     * 
     * @return The last fetched data, or null if no data has been fetched yet
     */
    public BoardOfDirectors getLastFetchedData() {
        return lastFetchedData;
    }
}