package com.recon.alert.model;

import java.time.Instant;

public record Alert(
        String transactionId,
        String sourcesSeen,
        String discrepancyDetails,
        Instant detectedAt,
        Instant receivedAt
) {
    public static Alert from(DiscrepancyEvent event, Instant receivedAt) {
        return new Alert(event.transactionId(), event.sourcesSeen(), event.discrepancyDetails(),
                event.detectedAt(), receivedAt);
    }
}
