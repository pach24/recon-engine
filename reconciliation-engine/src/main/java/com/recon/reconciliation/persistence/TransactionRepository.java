package com.recon.reconciliation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {

    Optional<TransactionRecord> findByTransactionIdAndSourceSystem(String transactionId, String sourceSystem);

    List<TransactionRecord> findByTransactionId(String transactionId);
}
