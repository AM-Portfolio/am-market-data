package com.am.marketdata.scraper.mapper;

import com.am.marketdata.common.model.NSEStockInsidicesData;
import com.am.marketdata.common.model.equity.StockInsidicesData;
import com.am.marketdata.common.model.equity.StockInsidicesData.Advance;
import com.am.marketdata.common.model.equity.StockInsidicesData.MarketStatus;
import com.am.marketdata.common.model.equity.StockInsidicesData.Metadata;
import com.am.marketdata.common.model.equity.StockInsidicesData.StockData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StockIndicesMapperTest {

    private StockIndicesMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mapper = new StockIndicesMapper();
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void testConvertToStockIndices() throws IOException {
        // Read test data from JSON file
        NSEStockInsidicesData testData = objectMapper.readValue(
            getClass().getResource("/data/stock-indices-data.json"),
            NSEStockInsidicesData.class
        );

        // Convert using mapper
        StockInsidicesData result = mapper.convertToStockIndices(testData);

        // Verify result
        assertNotNull(result);
        assertEquals("NIFTY 50", result.getName());

        // Verify advance data
        Advance advance = result.getAdvance();
        assertNotNull(advance);
        assertEquals(Integer.parseInt(testData.getAdvance().getDeclines()), advance.getDeclines());
        assertEquals(Integer.parseInt(testData.getAdvance().getAdvances()), advance.getAdvances());
        assertEquals(Integer.parseInt(testData.getAdvance().getUnchanged()), advance.getUnchanged());

        // Verify timestamp
        // LocalDateTime expectedTimestamp = LocalDateTime.parse("2025-03-23T11:09:07+05:30");
        // assertEquals(expectedTimestamp, result.getTimestamp());

        // Verify stock data list
        List<StockData> stockDataList = result.getData();
        assertNotNull(stockDataList);
        assertEquals(2, stockDataList.size());

        // Verify first stock data
        StockData firstStock = stockDataList.get(0);
        assertEquals(testData.getData().get(0).getSymbol(), firstStock.getSymbol());
        assertEquals(testData.getData().get(0).getOpen(), firstStock.getOpen(), 0.01);
        assertEquals(testData.getData().get(0).getDayHigh(), firstStock.getDayHigh(), 0.01);
        assertEquals(testData.getData().get(0).getDayLow(), firstStock.getDayLow(), 0.01);
        assertEquals(testData.getData().get(0).getLastPrice(), firstStock.getLastPrice(), 0.01);
        assertEquals(testData.getData().get(0).getPreviousClose(), firstStock.getPreviousClose(), 0.01);
        assertEquals(testData.getData().get(0).getChange(), firstStock.getChange(), 0.01);
        assertEquals(testData.getData().get(0).getPChange(), firstStock.getPChange(), 0.01);
        assertEquals(testData.getData().get(0).getTotalTradedVolume(), firstStock.getTotalTradedVolume());
        assertEquals(testData.getData().get(0).getTotalTradedValue(), firstStock.getTotalTradedValue(), 0.01);
        assertEquals(testData.getData().get(0).getYearHigh(), firstStock.getYearHigh(), 0.01);
        assertEquals(testData.getData().get(0).getYearLow(), firstStock.getYearLow(), 0.01);

        //Verify second recod with prioty 0
        StockData secondStock = stockDataList.get(1);
        assertEquals(testData.getData().get(1).getSymbol(), secondStock.getSymbol());
        assertEquals(testData.getData().get(1).getOpen(), secondStock.getOpen(), 0.01);
        assertEquals(testData.getData().get(1).getDayHigh(), secondStock.getDayHigh(), 0.01);
        assertEquals(testData.getData().get(1).getDayLow(), secondStock.getDayLow(), 0.01);
        assertEquals(testData.getData().get(1).getLastPrice(), secondStock.getLastPrice(), 0.01);
        assertEquals(testData.getData().get(1).getPreviousClose(), secondStock.getPreviousClose(), 0.01);
        assertEquals(testData.getData().get(1).getChange(), secondStock.getChange(), 0.01);
        assertEquals(testData.getData().get(1).getPChange(), secondStock.getPChange(), 0.01);
        assertEquals(testData.getData().get(1).getTotalTradedVolume(), secondStock.getTotalTradedVolume());
        assertEquals(testData.getData().get(1).getTotalTradedValue(), secondStock.getTotalTradedValue(), 0.01);
        assertEquals(testData.getData().get(1).getYearHigh(), secondStock.getYearHigh(), 0.01);
        assertEquals(testData.getData().get(1).getYearLow(), secondStock.getYearLow(), 0.01);
        assertEquals(testData.getData().get(1).getPerChange365d(), secondStock.getPerChange365d());
        assertEquals(testData.getData().get(1).getDate365dAgo(), secondStock.getDate365dAgo());
        assertEquals(testData.getData().get(1).getPerChange30d(), secondStock.getPerChange30d());
        assertEquals(testData.getData().get(1).getDate30dAgo(), secondStock.getDate30dAgo());

        // Verify metadata
        Metadata metadata = result.getMetadata();
        assertNotNull(metadata);
        assertEquals(testData.getMetadata().getIndexName(), metadata.getIndexName());
        assertEquals(testData.getMetadata().getOpen(), metadata.getOpen(), 0.01);
        assertEquals(testData.getMetadata().getHigh(), metadata.getHigh(), 0.01);
        assertEquals(testData.getMetadata().getLow(), metadata.getLow(), 0.01);
        assertEquals(testData.getMetadata().getPreviousClose(), metadata.getPreviousClose(), 0.01);
        assertEquals(testData.getMetadata().getChange(), metadata.getChange(), 0.01);
        assertEquals(testData.getMetadata().getPercChange(), metadata.getPercChange(), 0.01);

        // Verify market status
        MarketStatus marketStatus = result.getMarketStatus();
        assertNotNull(marketStatus);
        assertEquals(testData.getMarketStatus().getMarket(), marketStatus.getMarket());
        assertEquals(testData.getMarketStatus().getMarketStatus(), marketStatus.getMarketStatus());
        assertEquals(testData.getMarketStatus().getTradeDate(), marketStatus.getTradeDate());
        assertEquals(testData.getMarketStatus().getIndex(), marketStatus.getIndex());
        assertEquals(testData.getMarketStatus().getVariation(), marketStatus.getVariation(), 0.01);
        assertEquals(testData.getMarketStatus().getPercentChange(), marketStatus.getPercentChange(), 0.01);

        // Verify date fields
        assertEquals(testData.getDate30dAgo(), result.getDate30dAgo());
        assertEquals(testData.getDate365dAgo(), result.getDate365dAgo());
    }

    @Test
    void testConvertToStockIndices_NullData() {
        NSEStockInsidicesData testData = new NSEStockInsidicesData();
        testData.setData(null);

        StockInsidicesData result = mapper.convertToStockIndices(testData);
        assertNull(result);
    }

    @Test
    void testConvertToStockIndices_EmptyData() {
        NSEStockInsidicesData testData = new NSEStockInsidicesData();
        testData.setData(List.of());

        StockInsidicesData result = mapper.convertToStockIndices(testData);
        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void testConvertAdvance_NullInput() {
        NSEStockInsidicesData.Advance nullAdvance = null;
        Advance result = mapper.convertAdvance(nullAdvance);
        assertNull(result);
    }

    @Test
    void testConvertAdvance() {
        NSEStockInsidicesData.Advance testData = new NSEStockInsidicesData.Advance();
        testData.setDeclines("10");
        testData.setAdvances("20");
        testData.setUnchanged("5");

        Advance result = mapper.convertAdvance(testData);
        assertNotNull(result);
        assertEquals(10, result.getDeclines());
        assertEquals(20, result.getAdvances());
        assertEquals(5, result.getUnchanged());
    }

    @Test
    void testConvertStockDataList_NullInput() {
        List<NSEStockInsidicesData.StockData> nullList = null;
        List<StockData> result = mapper.convertStockDataList(nullList);
        assertNull(result);
    }
}
