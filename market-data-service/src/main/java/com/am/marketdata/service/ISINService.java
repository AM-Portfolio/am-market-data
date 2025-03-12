package com.am.marketdata.service;

import com.am.marketdata.config.ISINConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ISINService {
    private final ISINConfig isinConfig;

    @PostConstruct
    public void init() {
        List<String> isins = isinConfig.list();
        if (isins == null || isins.isEmpty()) {
            log.warn("No ISINs found in configuration during initialization");
        } else {
            log.info("Loaded {} ISINs during initialization", isins.size());
            log.debug("Loaded ISINs: {}", isins);
        }
    }

    public List<String> findDistinctIsins() {
        List<String> isins = isinConfig.list();
        if (isins == null || isins.isEmpty()) {
            log.warn("No ISINs found in configuration. Please check isin.yml configuration.");
            return Collections.emptyList();
        }
        log.debug("Found {} ISINs in configuration", isins.size());
        return isins;
    }
}
