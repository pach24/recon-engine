package com.recon.ingestion.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
        String transactionId,
        String sourceSystem,
        String accountId,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {
}
