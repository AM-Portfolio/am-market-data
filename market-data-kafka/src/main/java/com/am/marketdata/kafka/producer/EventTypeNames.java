package com.am.marketdata.kafka.producer;

public class EventTypeNames {
    
    // Market Data Events
    public static final String EQUITY_PRICE_UPDATE = "EQUITY_PRICE_UPDATE";
    public static final String STOCK_INDICES_UPDATE = "STOCK_INDICES_UPDATE";
    public static final String MARKET_INDICES_UPDATE = "MARKET_INDICES_UPDATE";
    
    // Corporate Actions Events
    public static final String BOARD_OF_DIRECTORS_UPDATE = "BOARD_OF_DIRECTORS_UPDATE";
    public static final String QUATERLY_FINANCIALS_UPDATE = "QUATERLY_FINANCIALS_UPDATE";
    
    private EventTypeNames() {
        throw new AssertionError("Cannot instantiate EventTypeNames");
    }
}
