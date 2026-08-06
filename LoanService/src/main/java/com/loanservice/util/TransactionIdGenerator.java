package com.loanservice.util;

import java.util.UUID;

/**
 * Utility class for generating transaction identifiers.
 */
public class TransactionIdGenerator {

    private TransactionIdGenerator() {}

    /**
     * Generates a random 24-character alphanumeric ID derived from a UUID.
     * Removes hyphens from a UUID and takes the first 24 hex characters.
     *
     * @return 24-character uppercase hex string (e.g. "A3F1C9B200DE4712F8901B3C")
     */
    public static String generate() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 24)
                .toUpperCase();
    }
}
