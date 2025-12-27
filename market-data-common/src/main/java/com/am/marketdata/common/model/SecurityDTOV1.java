package com.am.marketdata.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object for Security information
 * 
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityDTOV1 implements Serializable {
    private String symbol;
    private String isin;
    private String sector;
    private String industry;
    private Long marketCapValue;
    private String marketCapType;
}
