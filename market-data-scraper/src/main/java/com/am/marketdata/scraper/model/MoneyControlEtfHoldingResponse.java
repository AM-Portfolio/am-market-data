package com.am.marketdata.scraper.model;

import java.util.List;

public class MoneyControlEtfHoldingResponse {
    private List<Tab> tabs;
    private List<HoldingData> data;
    private boolean success;

    public List<Tab> getTabs() { return tabs; }
    public void setTabs(List<Tab> tabs) { this.tabs = tabs; }
    public List<HoldingData> getData() { return data; }
    public void setData(List<HoldingData> data) { this.data = data; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public static class Tab {
        private String name;
        private String apiKey;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }

    public static class HoldingData {
        private String name;
        private double holdingPer;
        private double investedAmount;
        private List<GraphData> graphData;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getHoldingPer() { return holdingPer; }
        public void setHoldingPer(double holdingPer) { this.holdingPer = holdingPer; }
        public double getInvestedAmount() { return investedAmount; }
        public void setInvestedAmount(double investedAmount) { this.investedAmount = investedAmount; }
        public List<GraphData> getGraphData() { return graphData; }
        public void setGraphData(List<GraphData> graphData) { this.graphData = graphData; }
    }

    public static class GraphData {
        private String month;
        private double value;
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
    }
}
