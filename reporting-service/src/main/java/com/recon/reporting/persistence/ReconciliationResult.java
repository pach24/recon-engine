package com.recon.reporting.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Read-only view of the reconciliation_results table owned and migrated by reconciliation-engine.
 */
@Entity
@Table(name = "reconciliation_results")
public class ReconciliationResult {

    @Id
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReconciliationStatus status;

    @Column(name = "sources_seen", nullable = false)
    private String sourcesSeen;

    @Column(name = "discrepancy_details")
    private String discrepancyDetails;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ReconciliationResult() {
    }

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public ReconciliationStatus getStatus() {
        return status;
    }

    public String getSourcesSeen() {
        return sourcesSeen;
    }

    public String getDiscrepancyDetails() {
        return discrepancyDetails;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
