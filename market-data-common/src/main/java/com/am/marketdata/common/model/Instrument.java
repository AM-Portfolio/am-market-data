package com.am.marketdata.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Common Instrument model representing a tradable entity.
 * Abstracts away provider-specific instrument details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instrument {

    private String instrumentToken; // Unique internal or provider ID
    private String exchangeToken; // Exchange specific ID
    private String tradingSymbol; // Symbol used for trading (e.g., RELIANCE)
    private String name; // Full company name
    private Double lastPrice; // Last traded price
    private Double tickSize; // Minimum price movement
    private String expiry; // Expiry date for derivatives
    private String instrumentType; // EQ, FUT, CE, PE
    private String segment; // NSE, BSE, NFO
    private String exchange; // NSE, BSE
    private Double strikePrice; // For options
    private Double lotSize; // Lot size for derivatives
    private String isin; // International Securities Identification Number
}
