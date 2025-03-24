package com.am.marketdata.scraper.service;

import com.am.marketdata.scraper.client.NSEApiClient;
import com.am.marketdata.scraper.config.ScraperConfig;
import com.am.marketdata.scraper.exception.CookieException;
import com.am.marketdata.scraper.model.CookieInfo;
import com.am.marketdata.scraper.model.WebsiteCookies;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.am.marketdata.scraper.service.CookieValidator;
import com.am.marketdata.scraper.service.CookieScraperService;
import com.am.marketdata.scraper.exception.CookieException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieScraperService {
    private final ScraperConfig scraperConfig;
    private final CookieValidator cookieValidator;
    
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    public WebsiteCookies scrapeCookies() {
        return scrapeCookies(scraperConfig.getUrls().stream().findFirst().get());
    }
    public WebsiteCookies scrapeCookies(String url) {
        log.info("Starting to scrape cookies from URL: {}", url);
        ChromeDriver webDriver = null;
        try {
            webDriver = scraperConfig.webDriver();
            
            // Set page load timeout
            webDriver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
            
            // Clear existing cookies before navigating
            webDriver.manage().deleteAllCookies();
            log.debug("Cleared existing cookies");

            // Navigate to the URL
            log.debug("Navigating to URL: {}", url);
            webDriver.get(url);

            // Wait for page to be in ready state
            waitForPageLoad(webDriver);
            log.debug("Page loaded successfully");

            // Get cookies after page load
            Set<Cookie> cookieSet = webDriver.manage().getCookies();
            List<Cookie> seleniumCookies = new ArrayList<>(cookieSet);
            log.info("Found {} cookies for URL: {}", seleniumCookies.size(), url);

            if (seleniumCookies.isEmpty()) {
                log.warn("No cookies found for URL: {}. This might indicate blocking or incorrect page load.", url);
                return WebsiteCookies.builder()
                        .websiteUrl(url)
                        .websiteName(webDriver.getTitle())
                        .build();
            }

            // Convert to CookieInfo and validate
            List<CookieInfo> cookies = seleniumCookies.stream()
                    .map(this::mapToCookieInfo)
                    .collect(Collectors.toList());

            // Validate cookies before proceeding
            String cookieString = cookies.stream()
                    .map(c -> c.getName() + "=" + c.getValue())
                    .collect(Collectors.joining("; "));

            Map<String, CookieValidator.ValidationResult> validationResults = cookieValidator.validateAllCookies(cookieString);
            
            // Check if all required cookies are valid
            boolean isValid = true;
            for (String requiredCookie : cookieValidator.getRequiredCookies()) {
                CookieValidator.ValidationResult result = validationResults.get(requiredCookie);
                if (result == null || !result.isValid()) {
                    log.warn("Required cookie {} is invalid: {}", requiredCookie, 
                            result == null ? "not found" : result.getMessage());
                    isValid = false;
                }
            }

            if (!isValid) {
                log.error("Cookie validation failed for URL: {}. Invalid cookies: {}", 
                        url, validationResults.values().stream()
                                .filter(r -> !r.isValid())
                                .map(r -> r.getMessage())
                                .collect(Collectors.joining(", ")));
                throw new CookieException("Cookie validation failed: Required cookies are invalid or missing");
            }

            // If validation passes, create WebsiteCookies object
            WebsiteCookies websiteCookies = WebsiteCookies.builder()
                    .websiteUrl(url)
                    .websiteName(webDriver.getTitle())
                    .cookies(cookies)
                    .build();
            
            // Generate and set the formatted cookie string
            websiteCookies.setCookiesString(websiteCookies.generateCookiesString());
            
            log.info("Successfully scraped and validated cookies for URL: {}", url);
            return websiteCookies;
                    
        } catch (TimeoutException e) {
            log.error("Timeout while loading URL: {}", url, e);
            throw new RuntimeException("Page load timeout for URL: " + url, e);
        } catch (CookieException e) {
            log.error("Cookie validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error scraping cookies for URL: {}", url, e);
            throw new RuntimeException("Error scraping cookies: " + e.getMessage(), e);
        }
    }

    private void waitForPageLoad(ChromeDriver driver) {
        new WebDriverWait(driver, WAIT_TIMEOUT)
                .until((ExpectedCondition<Boolean>) wd -> 
                        ((ChromeDriver) wd).executeScript("return document.readyState").equals("complete"));
    }

    private CookieInfo mapToCookieInfo(Cookie cookie) {
        return CookieInfo.builder()
                .name(cookie.getName())
                .value(cookie.getValue())
                .domain(cookie.getDomain())
                .path(cookie.getPath())
                .expiry(cookie.getExpiry().getTime())
                .secure(cookie.isSecure())
                .httpOnly(cookie.isHttpOnly())
                .build();
    }
}
