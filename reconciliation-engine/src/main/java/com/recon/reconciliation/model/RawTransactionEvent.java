package com.recon.reconciliation.model;

import java.math.BigDecimal;
import java.time.Instant;

public record RawTransactionEvent(
        String transactionId,
        String sourceSystem,
        String accountId,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {
}
