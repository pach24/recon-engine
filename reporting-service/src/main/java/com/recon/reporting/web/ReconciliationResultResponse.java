package com.recon.reporting.web;

import com.recon.reporting.persistence.ReconciliationResult;
import com.recon.reporting.persistence.ReconciliationStatus;

import java.time.Instant;

public record ReconciliationResultResponse(
        String transactionId,
        ReconciliationStatus status,
        String sourcesSeen,
        String discrepancyDetails,
        Instant updatedAt
) {
    public static ReconciliationResultResponse from(ReconciliationResult result) {
        return new ReconciliationResultResponse(
                result.getTransactionId(),
                result.getStatus(),
                result.getSourcesSeen(),
                result.getDiscrepancyDetails(),
                result.getUpdatedAt()
        );
    }
}
