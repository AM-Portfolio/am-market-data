package com.am.marketdata.scraper.service.common;

import com.am.marketdata.scraper.exception.MarketDataException;
import io.micrometer.core.instrument.Timer;

/**
 * Interface for processing market data
 * @param <T> The type of data to process
 * @param <R> The result type after processing
 */
public interface DataProcessor<T, R> {
    
    /**
     * Process the data
     * 
     * @param data The data to process
     * @return The processed result
     * @throws MarketDataException if processing fails
     */
    R process(T data) throws MarketDataException;
    
    /**
     * Get the data type name for logging and metrics
     * 
     * @return The data type name
     */
    String getDataTypeName();
    
    /**
     * Get the timer for measuring processing time
     * 
     * @return The timer
     */
    Timer getProcessingTimer();
}
