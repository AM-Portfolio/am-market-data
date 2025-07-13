package com.am.marketdata.mapper;

import com.am.common.investment.model.historical.HistoricalData;
import com.am.common.investment.model.historical.OHLCVTPoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper class to convert between Zerodha HistoricalData and AM Common HistoricalData models
 */
@Slf4j
@Component
public class HistoryDataMapper {

    /**
     * Convert a Zerodha historical data to AM common historical data model
     *
     * @param zerodhaHistoricalData Zerodha historical data model
     * @return AM common historical data model
     */
    public HistoricalData toCommonHistoricalData(com.zerodhatech.models.HistoricalData zerodhaHistoricalData) {
        if (zerodhaHistoricalData == null) {
            return null;
        }

        try {
            HistoricalData historicalData = new HistoricalData();
            List<OHLCVTPoint> dataPoints = new ArrayList<>();
            
            for (com.zerodhatech.models.HistoricalData data : zerodhaHistoricalData.dataArrayList) {
                OHLCVTPoint dataPoint = new OHLCVTPoint();
            
                dataPoint.setOpen(data.open);
                dataPoint.setHigh(data.high);
                dataPoint.setLow(data.low);
                dataPoint.setClose(data.close);
                dataPoint.setVolume(data.volume);

                
                dataPoints.add(dataPoint);
            }
            
            historicalData.setDataPoints(dataPoints);
            
            return historicalData;
        } catch (Exception e) {
            log.error("Error converting Zerodha historical data to common model: {}", e.getMessage(), e);
            return null;
        }
    }

}
