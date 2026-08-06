package com.loanservice.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Converts true → "Y" and false → "N" during JSON serialization.
 */
public class YNBooleanSerializer extends StdSerializer<Boolean> {

    public YNBooleanSerializer() {
        super(Boolean.class);
    }

    @Override
    public void serialize(Boolean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(Boolean.TRUE.equals(value) ? "Y" : "N");
        }
    }
}
