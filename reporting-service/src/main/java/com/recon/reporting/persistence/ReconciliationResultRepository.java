package com.recon.reporting.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, Long> {

    List<ReconciliationResult> findByStatus(ReconciliationStatus status);

    long countByStatus(ReconciliationStatus status);
}
