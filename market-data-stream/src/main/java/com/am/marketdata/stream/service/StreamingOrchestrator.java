package com.am.marketdata.stream.service;

import com.am.marketdata.common.model.MarketDataUpdateV1;
import com.am.marketdata.stream.websocket.MarketDataWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingOrchestrator {

    private final MarketDataWebSocketHandler webSocketHandler;

    public void publishUpdate(MarketDataUpdateV1 update) {
        log.debug("Publishing market data update: {}", update.getInstrumentKey());
        webSocketHandler.broadcast(update);
    }
}
