package com.am.marketdata.scraper.service.validator;

import com.am.marketdata.common.model.NSEIndicesResponse;
import com.am.marketdata.scraper.service.common.DataValidator;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator for NSE indices data
 */
@Slf4j
public class IndicesDataValidator implements DataValidator<NSEIndicesResponse> {
    
    @Override
    public boolean isValid(NSEIndicesResponse response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            log.warn("Received empty indices response");
            return false;
        }
        
        // Additional validation rules can be added here
        
        return true;
    }
    
    @Override
    public String getDataTypeName() {
        return "indices";
    }
}
