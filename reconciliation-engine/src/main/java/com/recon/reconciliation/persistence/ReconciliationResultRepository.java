package com.recon.reconciliation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, Long> {

    Optional<ReconciliationResult> findByTransactionId(String transactionId);
}
