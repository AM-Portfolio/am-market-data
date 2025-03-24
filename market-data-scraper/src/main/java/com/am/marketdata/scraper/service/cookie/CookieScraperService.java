package com.am.marketdata.scraper.service.cookie;

import com.am.marketdata.scraper.model.CookieInfo;
import com.am.marketdata.scraper.model.WebsiteCookies;
import com.am.marketdata.scraper.config.ScraperConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Retryable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieScraperService {
    private final ScraperConfig scraperConfig;
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    @Value("${nse.api.base-url:https://www.nseindia.com}")
    private String baseUrl;

    public WebsiteCookies scrapeCookies(String baseUrl) {
        this.baseUrl = baseUrl;
        return scrapeCookies();
    }

    // @Retryable(
    //     value = {NoSuchSessionException.class, WebDriverException.class},
    //     maxAttempts = 3,
    //     backoff = @Backoff(delay = 1000)
    // )
    public WebsiteCookies scrapeCookies() {
        log.info("Starting to scrape cookies from URL: {}", baseUrl);
        ChromeDriver webDriver = null;
        try {
            webDriver = scraperConfig.webDriver();
            
            // Set page load timeout
            webDriver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
            
            // Clear existing cookies before navigating
            webDriver.manage().deleteAllCookies();
            log.debug("Cleared existing cookies");

            // Navigate to the URL
            log.debug("Navigating to URL: {}", baseUrl);
            webDriver.get(baseUrl);

            // Wait for page to be in ready state
            waitForPageLoad(webDriver);
            log.debug("Page loaded successfully");

            // Get cookies after page load
            Set<Cookie> cookieSet = webDriver.manage().getCookies();
            List<Cookie> seleniumCookies = new ArrayList<>(cookieSet);
            log.info("Found {} cookies for URL: {}", seleniumCookies.size(), baseUrl);

            if (seleniumCookies.isEmpty()) {
                log.warn("No cookies found for URL: {}. This might indicate blocking or incorrect page load.", baseUrl);
            }

            List<CookieInfo> cookies = seleniumCookies.stream()
                    .map(this::mapToCookieInfo)
                    .collect(Collectors.toList());

            String title = webDriver.getTitle();
            log.debug("Page title: {}", title);

            // Create WebsiteCookies object with raw cookie string
            WebsiteCookies websiteCookies = WebsiteCookies.builder()
                    .websiteUrl(baseUrl)
                    .websiteName(title)
                    .cookies(cookies)
                    .build();
            
            // Generate and set the formatted cookie string
            websiteCookies.setCookiesString(websiteCookies.generateCookiesString());
            
            return websiteCookies;
                    
        } catch (TimeoutException e) {
            log.error("Timeout while loading URL: " + baseUrl, e);
            throw new RuntimeException("Page load timeout for URL: " + baseUrl, e);
        } catch (NoSuchSessionException e) {
            log.error("Invalid session while scraping URL: " + baseUrl + ". Will retry with new session.", e);
            throw e; // Let @Retryable handle it
        } catch (WebDriverException e) {
            log.error("WebDriver error while scraping cookies from URL: " + baseUrl, e);
            throw e; // Let @Retryable handle it
        } catch (Exception e) {
            log.error("Unexpected error while scraping cookies from URL: " + baseUrl, e);
            throw new RuntimeException("Failed to scrape cookies from: " + baseUrl, e);
        }
    }

    private void waitForPageLoad(ChromeDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    return "complete".equals(
                        ((ChromeDriver) driver).executeScript("return document.readyState")
                    );
                } catch (Exception e) {
                    log.debug("Error checking page state: {}", e.getMessage());
                    return false;
                }
            }
        };
        wait.until(pageLoadCondition);
    }

    private CookieInfo mapToCookieInfo(Cookie cookie) {
        return CookieInfo.builder()
                .name(cookie.getName())
                .value(cookie.getValue())
                .domain(cookie.getDomain())
                .path(cookie.getPath())
                .secure(cookie.isSecure())
                .httpOnly(cookie.isHttpOnly())
                .expiry(cookie.getExpiry() != null ? cookie.getExpiry().getTime() : null)
                .build();
    }
}
