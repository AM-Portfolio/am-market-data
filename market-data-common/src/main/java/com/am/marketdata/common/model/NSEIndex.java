package com.am.marketdata.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.am.marketdata.common.serializer.SafeDoubleDeserializer;
import lombok.Data;

@Data
public class NSEIndex {
    private String key;
    private String index;
    private String indexSymbol;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double last;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double variation;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double percentChange;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double open;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double high;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double low;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double previousClose;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double yearHigh;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double yearLow;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double indicativeClose;
    
    private String pe;
    private String pb;
    private String dy;
    private String declines;
    private String advances;
    private String unchanged;
    
    @JsonProperty("perChange365d")
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double percentChange365d;
    
    private String date365dAgo;
    private String chart365dPath;
    private String date30dAgo;
    
    @JsonProperty("perChange30d")
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double percentChange30d;
    
    private String chart30dPath;
    private String chartTodayPath;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double previousDay;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double oneWeekAgo;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double oneMonthAgo;
    
    @JsonDeserialize(using = SafeDoubleDeserializer.class)
    private double oneYearAgo;
}
