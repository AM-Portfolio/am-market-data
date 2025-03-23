package com.am.marketdata.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NSEIndicesResponse {
    private List<NSEIndex> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NSEIndex {
        private String index;
        private String variation;
        private String last;
        private String previousDay;
        private String open;
        private String high;
        private String low;
        private String yearHigh;
        private String yearLow;
        private String percentChange;
        private String advances;
        private String declines;
        private String unchanged;
        private String pe;
        private String pb;
        private String dy;
        private String oneWeekAgo;
        private String oneMonthAgo;
        private String oneYearAgo;
        private String date30dAgo;
        private String date365dAgo;
        private String chartTodayPath;
        private String chart30dPath;
        private String chart365dPath;
        private String percentChange30d;
        private String percentChange365d;
        private String perChange365d;
        private String key;
        private String indexSymbol;
    }
}
