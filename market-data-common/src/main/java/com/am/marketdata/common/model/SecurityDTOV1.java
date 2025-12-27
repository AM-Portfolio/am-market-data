package com.am.marketdata.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object for Security information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityDTO implements Serializable {
    private String symbol;
    private String isin;
    private String sector;
    private String industry;
    private Long marketCapValue;
    private String marketCapType;
}
