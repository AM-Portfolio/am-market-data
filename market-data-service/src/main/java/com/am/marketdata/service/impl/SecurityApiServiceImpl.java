package com.am.marketdata.service.impl;

import com.am.marketdata.api.service.SecurityApiService;
import com.am.marketdata.common.model.SecurityDTOV1;
import com.am.marketdata.common.model.SecuritySearchRequestV1;
import com.am.marketdata.service.SecurityService;
import com.am.marketdata.service.model.security.SecurityDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of SecurityApiService that delegates to SecurityService
 */
@Service
@RequiredArgsConstructor
public class SecurityApiServiceImpl implements SecurityApiService {

    private final SecurityService securityService;

    @Override
    public List<SecurityDTOV1> findBySymbols(List<String> symbols) {
        return securityService.findBySymbols(symbols).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> getSymbolToSectorMap(List<String> symbols) {
        return securityService.getSymbolToSectorMap(symbols);
    }

    @Override
    public List<SecurityDTOV1> search(SecuritySearchRequestV1 request) {
        // We need to map common DTO search request to service search request if they
        // differ
        // For now, they are identical in structure
        com.am.marketdata.service.dto.SecuritySearchRequest serviceRequest = com.am.marketdata.service.dto.SecuritySearchRequest
                .builder()
                .symbols(request.getSymbols())
                .isin(request.getIsin())
                .sector(request.getSector())
                .industry(request.getIndustry())
                .index(request.getIndex())
                .query(request.getQuery())
                .build();

        return securityService.search(serviceRequest).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SecurityDTOV1> getAllSecurities() {
        return securityService.getAllSecurities().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private SecurityDTOV1 mapToDTO(SecurityDocument doc) {
        if (doc == null)
            return null;
        return SecurityDTOV1.builder()
                .symbol(doc.getKey() != null ? doc.getKey().getSymbol() : null)
                .isin(doc.getKey() != null ? doc.getKey().getIsin() : null)
                .sector(doc.getMetadata() != null ? doc.getMetadata().getSector() : null)
                .industry(doc.getMetadata() != null ? doc.getMetadata().getIndustry() : null)
                .marketCapValue(doc.getMetadata() != null ? doc.getMetadata().getMarketCapValue() : null)
                .marketCapType(doc.getMetadata() != null ? doc.getMetadata().getMarketCapType() : null)
                .build();
    }
}
