package com.recon.alert.model;

import java.time.Instant;

public record DiscrepancyEvent(
        String transactionId,
        String sourcesSeen,
        String discrepancyDetails,
        Instant detectedAt
) {
}
