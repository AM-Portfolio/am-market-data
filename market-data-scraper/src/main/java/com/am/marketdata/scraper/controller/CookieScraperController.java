package com.am.marketdata.scraper.controller;

import com.am.marketdata.scraper.config.ScraperConfig;
import com.am.marketdata.scraper.model.WebsiteCookies;
import com.am.marketdata.scraper.service.cookie.CookieScraperService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scraper")
@RequiredArgsConstructor
public class CookieScraperController {
    private final CookieScraperService scraperService;
    private final ScraperConfig scraperConfig;

    @GetMapping("/cookies")
    public List<WebsiteCookies> scrapeCookies() {
        return scraperConfig.getUrls().stream()
                .map(scraperService::scrapeCookies)
                .collect(Collectors.toList());
    }
}
