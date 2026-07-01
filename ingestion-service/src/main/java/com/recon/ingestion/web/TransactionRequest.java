package com.recon.ingestion.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionRequest(
        @NotBlank String transactionId,
        @NotBlank String sourceSystem,
        @NotBlank String accountId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        @NotNull Instant occurredAt
) {
}
