package com.am.marketdata.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for JSON operations, particularly focused on model deserialization
 * with proper error handling and logging.
 */
@Slf4j
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Deserialize JSON string into a specified model class
     *
     * @param json JSON string to deserialize
     * @param clazz Target model class
     * @param <T> Type parameter for the target model
     * @return Deserialized model instance or null if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            log.debug("Problematic JSON content: {}", json);
            return null;
        }
    }

    /**
     * Read JSON from classpath resource and deserialize into a specified model class
     *
     * @param resourcePath Path to the resource file in classpath
     * @param clazz Target model class
     * @param <T> Type parameter for the target model
     * @return Deserialized model instance or null if reading/deserialization fails
     */
    public static <T> T fromResource(String resourcePath, Class<T> clazz) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStream is = resource.getInputStream()) {
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                return fromJson(json, clazz);
            }
        } catch (IOException e) {
            log.error("Failed to read resource {}: {}", resourcePath, e.getMessage());
            return null;
        }
    }

    /**
     * Serialize model to JSON string
     *
     * @param model Model instance to serialize
     * @return JSON string or null if serialization fails
     */
    public static String toJson(Object model) {
        try {
            return objectMapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize {} to JSON: {}", 
                model.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private JsonUtils() {
        // Prevent instantiation
    }
}
