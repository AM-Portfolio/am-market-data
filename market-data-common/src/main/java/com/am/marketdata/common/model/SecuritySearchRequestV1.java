package com.am.marketdata.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Request DTO for searching securities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecuritySearchRequest implements Serializable {
    private List<String> symbols;
    private String isin;
    private String sector;
    private String industry;
    private String index;
    private String query;
}
