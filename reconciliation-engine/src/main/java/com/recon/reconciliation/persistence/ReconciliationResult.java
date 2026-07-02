package com.recon.reconciliation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reconciliation_results")
public class ReconciliationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true)
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

    public ReconciliationResult(String transactionId, ReconciliationStatus status,
                                 String sourcesSeen, String discrepancyDetails, Instant updatedAt) {
        this.transactionId = transactionId;
        this.status = status;
        this.sourcesSeen = sourcesSeen;
        this.discrepancyDetails = discrepancyDetails;
        this.updatedAt = updatedAt;
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

    public void update(ReconciliationStatus status, String sourcesSeen, String discrepancyDetails, Instant updatedAt) {
        this.status = status;
        this.sourcesSeen = sourcesSeen;
        this.discrepancyDetails = discrepancyDetails;
        this.updatedAt = updatedAt;
    }
}
