package com.am.marketdata.provider.upstox.service;

import com.am.marketdata.provider.upstox.model.UpstoxInstrument;
import com.am.marketdata.provider.upstox.repo.UpstoxInstrumentRepository;
import com.am.marketdata.provider.service.InstrumentDataProvider;
import com.am.marketdata.provider.dto.InstrumentSearchCriteria;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpstoxInstrumentService implements InstrumentDataProvider {

    private final UpstoxInstrumentRepository instrumentRepository;
    private final ObjectMapper objectMapper;
    private final org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    private static final int BATCH_SIZE = 1000;

    @Override
    public String getProviderName() {
        return "UPSTOX";
    }

    @Override
    public void updateInstruments(String filePath) throws IOException {
        updateInstrumentsFromFile(filePath);
    }

    public void updateInstrumentsFromFile(String filePath) throws IOException {
        log.info("Starting InstrumentV1 update from file: {}", filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        JsonFactory jsonFactory = objectMapper.getFactory();
        try (JsonParser jsonParser = jsonFactory.createParser(file)) {

            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected content to be an array");
            }

            List<UpstoxInstrument> batch = new ArrayList<>();
            int totalProcessed = 0;

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                UpstoxInstrument instrument = objectMapper.readValue(jsonParser, UpstoxInstrument.class);

                if (instrument != null) {
                    if (instrument.getIsin() == null || instrument.getIsin().isEmpty()) {
                        String isin = extractIsin(instrument.getInstrumentKey());
                        if (isin == null) {
                            isin = extractIsin(instrument.getUnderlyingKey());
                        }
                        instrument.setIsin(isin);
                    }
                    batch.add(instrument);
                }

                if (batch.size() >= BATCH_SIZE) {
                    saveBatch(batch);
                    totalProcessed += batch.size();
                    batch.clear();
                    log.info("Processed {} instruments...", totalProcessed);
                }
            }

            if (!batch.isEmpty()) {
                saveBatch(batch);
                totalProcessed += batch.size();
            }

            log.info("Finished updating instruments. Total processed: {}", totalProcessed);
        }
    }

    private void saveBatch(List<UpstoxInstrument> batch) {
        try {
            instrumentRepository.saveAll(batch);
        } catch (Exception e) {
            log.error("Error saving batch of size {}", batch.size(), e);
        }
    }

    @Override
    public List<UpstoxInstrument> searchInstruments(InstrumentSearchCriteria criteria) {
        org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
        List<org.springframework.data.mongodb.core.query.Criteria> criteriaList = new ArrayList<>();

        if (criteria.getExchanges() != null && !criteria.getExchanges().isEmpty()) {
            criteriaList.add(
                    org.springframework.data.mongodb.core.query.Criteria.where("exchange").in(criteria.getExchanges()));
        }
        if (criteria.getInstrumentTypes() != null && !criteria.getInstrumentTypes().isEmpty()) {
            criteriaList.add(org.springframework.data.mongodb.core.query.Criteria.where("instrumentType")
                    .in(criteria.getInstrumentTypes()));
        }
        if (criteria.getSegments() != null && !criteria.getSegments().isEmpty()) {
            criteriaList.add(
                    org.springframework.data.mongodb.core.query.Criteria.where("segment").in(criteria.getSegments()));
        }
        if (criteria.getWeekly() != null) {
            criteriaList
                    .add(org.springframework.data.mongodb.core.query.Criteria.where("weekly").is(criteria.getWeekly()));
        }
        if (criteria.getIsins() != null && !criteria.getIsins().isEmpty()) {
            criteriaList
                    .add(org.springframework.data.mongodb.core.query.Criteria.where("isin").in(criteria.getIsins()));
        }
        if (criteria.getTradingSymbols() != null && !criteria.getTradingSymbols().isEmpty()) {
            criteriaList.add(org.springframework.data.mongodb.core.query.Criteria.where("tradingSymbol")
                    .in(criteria.getTradingSymbols()));
        }

        if (criteria.getQueries() != null && !criteria.getQueries().isEmpty()) {
            List<org.springframework.data.mongodb.core.query.Criteria> orCriteria = new ArrayList<>();
            orCriteria
                    .add(org.springframework.data.mongodb.core.query.Criteria.where("isin").in(criteria.getQueries()));
            orCriteria.add(org.springframework.data.mongodb.core.query.Criteria.where("assetSymbol")
                    .in(criteria.getQueries()));

            for (String text : criteria.getQueries()) {
                String regex = ".*" + java.util.regex.Pattern.quote(text) + ".*";
                orCriteria.add(org.springframework.data.mongodb.core.query.Criteria.where("name").regex(regex, "i"));
                orCriteria.add(
                        org.springframework.data.mongodb.core.query.Criteria.where("assetSymbol").regex(regex, "i"));
            }

            if (!orCriteria.isEmpty()) {
                criteriaList.add(new org.springframework.data.mongodb.core.query.Criteria()
                        .orOperator(orCriteria.toArray(new org.springframework.data.mongodb.core.query.Criteria[0])));
            }
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new org.springframework.data.mongodb.core.query.Criteria()
                    .andOperator(criteriaList.toArray(new org.springframework.data.mongodb.core.query.Criteria[0])));
        } else {
            query.limit(100);
        }

        return mongoTemplate.find(query, UpstoxInstrument.class);
    }

    private String extractIsin(String key) {
        if (key != null && key.contains("|")) {
            String[] parts = key.split("\\|");
            if (parts.length > 1) {
                return parts[1];
            }
        }
        return null;
    }
}
