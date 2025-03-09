package com.am.marketdata.scraper.model;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class WebsiteCookies {
    private String websiteUrl;
    private String websiteName;
    private List<CookieInfo> cookies;
}
