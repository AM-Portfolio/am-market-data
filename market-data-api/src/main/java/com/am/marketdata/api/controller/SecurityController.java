package com.am.marketdata.api.controller;

import com.am.marketdata.api.service.SecurityApiService;
import com.am.marketdata.common.model.SecurityDTO;
import com.am.marketdata.common.model.SecuritySearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Controller for Security metadata and search
 */
@RestController
@RequestMapping("/api/v1/security")
@Tag(name = "Security Metadata", description = "Endpoints for searching and retrieving security metadata (sector, industry, market cap)")
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityApiService securityApiService;

    @GetMapping("/find")
    @Operation(summary = "Find securities by symbols", description = "Returns metadata for specific symbols")
    public ResponseEntity<List<SecurityDTO>> findBySymbols(@RequestParam String symbols) {
        List<String> symbolList = Arrays.asList(symbols.split(","));
        return ResponseEntity.ok(securityApiService.findBySymbols(symbolList));
    }

    @GetMapping("/sectors")
    @Operation(summary = "Get sectors for symbols", description = "Returns a mapping of symbol to sector")
    public ResponseEntity<Map<String, String>> getSectors(@RequestParam String symbols) {
        List<String> symbolList = Arrays.asList(symbols.split(","));
        return ResponseEntity.ok(securityApiService.getSymbolToSectorMap(symbolList));
    }

    @PostMapping("/search")
    @Operation(summary = "Search securities", description = "Search for securities using various filters")
    public ResponseEntity<List<SecurityDTO>> search(@RequestBody SecuritySearchRequest request) {
        return ResponseEntity.ok(securityApiService.search(request));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all securities", description = "Returns all available securities in the database")
    public ResponseEntity<List<SecurityDTO>> getAll() {
        return ResponseEntity.ok(securityApiService.getAllSecurities());
    }
}
