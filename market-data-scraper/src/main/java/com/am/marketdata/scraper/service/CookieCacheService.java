package com.am.marketdata.scraper.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CookieCacheService {
    private static final String NSE_COOKIE_KEY = "nse_cookies";
    private final Cache<String, String> cookieCache;

    public CookieCacheService() {
        this.cookieCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(1))
            .build();
    }

    public void storeCookies(String cookies) {
        cookieCache.put(NSE_COOKIE_KEY, cookies);
        log.info("Updated cookies in cache: {}", maskCookieValues(cookies));
    }

    public String getCookies() {
        String cookies = cookieCache.getIfPresent(NSE_COOKIE_KEY);
        if (cookies != null) {
            log.debug("Retrieved cookies from cache: {}", maskCookieValues(cookies));
        }
        return cookies;
    }

    public void invalidateCookies() {
        String cookies = cookieCache.getIfPresent(NSE_COOKIE_KEY);
        if (cookies != null) {
            log.info("Invalidating cached cookies: {}", maskCookieValues(cookies));
            cookieCache.invalidate(NSE_COOKIE_KEY);
        }
    }

    private String maskCookieValues(String cookies) {
        if (cookies == null) return null;
        // Split cookies and mask values while preserving names
        return Stream.of(cookies.split(";"))
            .map(cookie -> {
                String[] parts = cookie.split("=", 2);
                return parts.length > 1 
                    ? parts[0].trim() + "=*****" 
                    : cookie.trim() + "=*****";
            })
            .collect(Collectors.joining("; "));
    }
}
