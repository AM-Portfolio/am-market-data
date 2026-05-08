package com.am.marketdata.service.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpstoxInstrumentService {

    private static final Logger log = LoggerFactory.getLogger(UpstoxInstrumentService.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    private MongoClient mongoClient;
    private MongoDatabase database;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing direct MongoDB client for Upstox instrument lookup");
            this.mongoClient = MongoClients.create(mongoUri);
            this.database = mongoClient.getDatabase("market_data");
        } catch (Exception e) {
            log.error("Failed to initialize MongoDB client: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    /**
     * Get the Upstox instrument key (EXCHANGE|ISIN) for a given trading symbol.
     * Searches for NSE_EQ segment by default.
     */
    public Optional<String> getInstrumentKey(String tradingSymbol) {
        if (database == null) {
            return Optional.empty();
        }
        
        try {
            MongoCollection<Document> collection = database.getCollection("upstock_instruments");
            Document instrument = collection.find(Filters.and(
                    Filters.eq("trading_symbol", tradingSymbol.toUpperCase()),
                    Filters.eq("segment", "NSE_EQ")
            )).first();
            
            if (instrument != null && instrument.containsKey("_id")) {
                String key = instrument.get("_id").toString();
                log.debug("Found Upstox instrument key for {}: {}", tradingSymbol, key);
                return Optional.of(key);
            }
            
            log.warn("Could not find Upstox instrument key for {} in segment NSE_EQ", tradingSymbol);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error looking up Upstox instrument for {}: {}", tradingSymbol, e.getMessage());
            return Optional.empty();
        }
    }
}
