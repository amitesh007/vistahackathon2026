package com.loanservice.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanservice.entity.LoanPrincipalPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for YNBooleanDeserializer and YNBooleanSerializer.
 * Tests round-trip conversion: JSON "Y"/"N" ↔ Boolean true/false.
 */
class YNBooleanConverterTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        com.fasterxml.jackson.datatype.jsr310.JavaTimeModule module =
                new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule();
        objectMapper.registerModule(module);
    }

    // ---- Deserializer tests ----

    @Test
    @DisplayName("Deserializer: 'Y' → true")
    void deserializer_Y_becomesTrue() throws Exception {
        String json = "{\"className\":\"Test\",\"suppressBreakfunding\":\"Y\"}";
        LoanRequest req = objectMapper.readValue(json, LoanRequest.class);
        assertTrue(req.getSuppressBreakfunding());
    }

    @Test
    @DisplayName("Deserializer: 'N' → false")
    void deserializer_N_becomesFalse() throws Exception {
        String json = "{\"className\":\"Test\",\"suppressBreakfunding\":\"N\"}";
        LoanRequest req = objectMapper.readValue(json, LoanRequest.class);
        assertFalse(req.getSuppressBreakfunding());
    }

    @Test
    @DisplayName("Deserializer: lowercase 'y' → true")
    void deserializer_lowercase_y_becomesTrue() throws Exception {
        String json = "{\"className\":\"Test\",\"suppressBreakfunding\":\"y\"}";
        LoanRequest req = objectMapper.readValue(json, LoanRequest.class);
        assertTrue(req.getSuppressBreakfunding());
    }

    @Test
    @DisplayName("Deserializer: lowercase 'n' → false")
    void deserializer_lowercase_n_becomesFalse() throws Exception {
        String json = "{\"className\":\"Test\",\"suppressBreakfunding\":\"n\"}";
        LoanRequest req = objectMapper.readValue(json, LoanRequest.class);
        assertFalse(req.getSuppressBreakfunding());
    }

    @Test
    @DisplayName("Deserializer: empty string → null")
    void deserializer_emptyString_becomesNull() throws Exception {
        String json = "{\"className\":\"Test\",\"suppressBreakfunding\":\"\"}";
        LoanRequest req = objectMapper.readValue(json, LoanRequest.class);
        assertNull(req.getSuppressBreakfunding());
    }

    @Test
    @DisplayName("Deserializer: whitespace string → null")
    void deserializer_whitespace_becomesNull() throws Exception {
        String json = "{\"className\":\"Test\",\"suppressBreakfunding\":\"  \"}";
        LoanRequest req = objectMapper.readValue(json, LoanRequest.class);
        assertNull(req.getSuppressBreakfunding());
    }

    @Test
    @DisplayName("Deserializer: invalid value throws IllegalArgumentException")
    void deserializer_invalidValue_throwsException() {
        String json = "{\"className\":\"Test\",\"suppressBreakfunding\":\"X\"}";
        assertThrows(Exception.class, () -> objectMapper.readValue(json, LoanRequest.class));
    }

    @Test
    @DisplayName("Deserializer: applies to preventOnlineDeletionIndicator field")
    void deserializer_preventOnlineDeletionIndicator_Y() throws Exception {
        String json = "{\"className\":\"Test\",\"preventOnlineDeletionIndicator\":\"Y\"}";
        LoanRequest req = objectMapper.readValue(json, LoanRequest.class);
        assertTrue(req.getPreventOnlineDeletionIndicator());
    }

    @Test
    @DisplayName("Deserializer: applies to applyToEarliestItem field")
    void deserializer_applyToEarliestItem_N() throws Exception {
        String json = "{\"className\":\"Test\",\"applyToEarliestItem\":\"N\"}";
        LoanRequest req = objectMapper.readValue(json, LoanRequest.class);
        assertFalse(req.getApplyToEarliestItem());
    }

    @Test
    @DisplayName("Deserializer: applies to autoReduceFacility field")
    void deserializer_autoReduceFacility_Y() throws Exception {
        String json = "{\"className\":\"Test\",\"autoReduceFacility\":\"Y\"}";
        LoanRequest req = objectMapper.readValue(json, LoanRequest.class);
        assertTrue(req.getAutoReduceFacility());
    }

    // ---- Serializer tests (via entity) ----

    @Test
    @DisplayName("Serializer: true → 'Y' in JSON output")
    void serializer_true_becomesY() throws Exception {
        LoanPrincipalPayment entity = new LoanPrincipalPayment();
        entity.setSuppressBreakfunding(Boolean.TRUE);
        String json = objectMapper.writeValueAsString(entity);
        assertTrue(json.contains("\"suppressBreakfunding\":\"Y\""),
                "Expected 'Y' in output but got: " + json);
    }

    @Test
    @DisplayName("Serializer: false → 'N' in JSON output")
    void serializer_false_becomesN() throws Exception {
        LoanPrincipalPayment entity = new LoanPrincipalPayment();
        entity.setSuppressBreakfunding(Boolean.FALSE);
        String json = objectMapper.writeValueAsString(entity);
        assertTrue(json.contains("\"suppressBreakfunding\":\"N\""),
                "Expected 'N' in output but got: " + json);
    }

    @Test
    @DisplayName("Serializer: null → null in JSON output")
    void serializer_null_becomesNull() throws Exception {
        LoanPrincipalPayment entity = new LoanPrincipalPayment();
        entity.setSuppressBreakfunding(null);
        String json = objectMapper.writeValueAsString(entity);
        assertTrue(json.contains("\"suppressBreakfunding\":null"),
                "Expected null in output but got: " + json);
    }

    @Test
    @DisplayName("Serializer: preventOnlineDeletionIndicator true → 'Y'")
    void serializer_preventOnlineDeletionIndicator_Y() throws Exception {
        LoanPrincipalPayment entity = new LoanPrincipalPayment();
        entity.setPreventOnlineDeletionIndicator(Boolean.TRUE);
        String json = objectMapper.writeValueAsString(entity);
        assertTrue(json.contains("\"preventOnlineDeletionIndicator\":\"Y\""),
                "Expected 'Y' in output but got: " + json);
    }

    @Test
    @DisplayName("Serializer: autoReduceFacility false → 'N'")
    void serializer_autoReduceFacility_N() throws Exception {
        LoanPrincipalPayment entity = new LoanPrincipalPayment();
        entity.setAutoReduceFacility(Boolean.FALSE);
        String json = objectMapper.writeValueAsString(entity);
        assertTrue(json.contains("\"autoReduceFacility\":\"N\""),
                "Expected 'N' in output but got: " + json);
    }
}
