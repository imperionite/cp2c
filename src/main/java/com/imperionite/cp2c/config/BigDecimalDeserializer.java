package com.imperionite.cp2c.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.math.BigDecimal;

// Custom deserializer to handle potential empty strings or nulls for BigDecimal fields
public class BigDecimalDeserializer extends JsonDeserializer<BigDecimal> {
    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO; // Return 0 or null based on your preference for empty values
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            // Log the error or throw a more specific exception if needed
            System.err.println("Failed to deserialize '" + value + "' to BigDecimal. Returning ZERO. Error: " + e.getMessage());
            return BigDecimal.ZERO; // Return 0 on parsing error
        }
    }
}