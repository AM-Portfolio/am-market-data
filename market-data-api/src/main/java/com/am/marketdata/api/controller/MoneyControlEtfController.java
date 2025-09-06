package com.am.marketdata.api.controller;

import com.am.marketdata.scraper.client.MoneyControlApiClient;
import com.am.marketdata.scraper.model.MoneyControlEtfHoldingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/moneycontrol/etf")
@RequiredArgsConstructor
public class MoneyControlEtfController {
    private final MoneyControlApiClient moneyControlApiClient;

    @PostMapping("/holdings")
    public ResponseEntity<List<MoneyControlEtfHoldingResponse>> getEtfHoldings(@RequestBody List<String> isinList) throws ExecutionException, InterruptedException {
        List<CompletableFuture<MoneyControlEtfHoldingResponse>> futures = isinList.stream()
                .map(isin -> CompletableFuture.supplyAsync(() -> moneyControlApiClient.fetchEtfHoldings(isin)))
                .collect(Collectors.toList());
        List<MoneyControlEtfHoldingResponse> responses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}

@Configuration
@EnableAsync
class AsyncConfig {}
