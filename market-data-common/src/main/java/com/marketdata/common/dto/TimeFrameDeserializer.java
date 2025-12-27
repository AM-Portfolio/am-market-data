package com.marketdata.common.dto;

import com.am.marketdata.common.model.TimeFrameV1;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Custom JSON deserializer for TimeFrameV1 that accepts both:
 * - Enum names: "DAY", "HOUR", "MINUTE", etc.
 * - API values: "1D", "1H", "5m", etc.
 */
public class TimeFrameDeserializer extends JsonDeserializer<TimeFrameV1> {

    @Override
    public TimeFrameV1 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.trim().isEmpty()) {
            return TimeFrameV1.MINUTE; // Default value
        }

        try {
            return TimeFrameV1.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid TimeFrameV1 value: " + value +
                    ". Expected enum name (DAY, HOUR, MINUTE) or API value (1D, 1H, 5m)", e);
        }
    }
}
