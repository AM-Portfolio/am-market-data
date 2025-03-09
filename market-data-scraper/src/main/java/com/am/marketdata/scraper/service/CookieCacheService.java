package com.am.marketdata.scraper.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
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
    }

    public String getCookies() {
        return cookieCache.getIfPresent(NSE_COOKIE_KEY);
    }

    public void invalidateCookies() {
        cookieCache.invalidate(NSE_COOKIE_KEY);
    }
}
