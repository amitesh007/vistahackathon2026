package com.loanservice.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Converts "Y" / "y" → true and "N" / "n" → false during JSON deserialization.
 */
public class YNBooleanDeserializer extends StdDeserializer<Boolean> {

    public YNBooleanDeserializer() {
        super(Boolean.class);
    }

    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String value = p.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.trim().toUpperCase()) {
            case "Y" -> true;
            case "N" -> false;
            default -> throw new IllegalArgumentException(
                    "Invalid Y/N value '" + value + "' for field '" + p.currentName() + "'. Expected 'Y', 'N', or empty.");
        };
    }
}
