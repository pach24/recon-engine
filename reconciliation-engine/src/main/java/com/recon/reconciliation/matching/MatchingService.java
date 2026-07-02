package com.recon.reconciliation.matching;

import com.recon.reconciliation.kafka.DiscrepancyProducer;
import com.recon.reconciliation.model.DiscrepancyEvent;
import com.recon.reconciliation.persistence.ReconciliationResult;
import com.recon.reconciliation.persistence.ReconciliationResultRepository;
import com.recon.reconciliation.persistence.ReconciliationStatus;
import com.recon.reconciliation.persistence.TransactionRecord;
import com.recon.reconciliation.persistence.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final TransactionRepository transactionRepository;
    private final ReconciliationResultRepository reconciliationResultRepository;
    private final DiscrepancyProducer discrepancyProducer;
    private final BigDecimal amountTolerance;

    public MatchingService(TransactionRepository transactionRepository,
                            ReconciliationResultRepository reconciliationResultRepository,
                            DiscrepancyProducer discrepancyProducer,
                            @Value("${app.matching.amount-tolerance:0.01}") BigDecimal amountTolerance) {
        this.transactionRepository = transactionRepository;
        this.reconciliationResultRepository = reconciliationResultRepository;
        this.discrepancyProducer = discrepancyProducer;
        this.amountTolerance = amountTolerance;
    }

    @Transactional
    public void reconcile(String transactionId) {
        List<TransactionRecord> records = transactionRepository.findByTransactionId(transactionId);
        String sourcesSeen = records.stream()
                .map(TransactionRecord::getSourceSystem)
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));

        ReconciliationStatus status;
        String discrepancyDetails = null;

        long distinctSources = records.stream().map(TransactionRecord::getSourceSystem).distinct().count();
        if (distinctSources < 2) {
            status = ReconciliationStatus.PENDING;
        } else {
            List<String> mismatches = findMismatches(records);
            if (mismatches.isEmpty()) {
                status = ReconciliationStatus.MATCHED;
            } else {
                status = ReconciliationStatus.DISCREPANCY;
                discrepancyDetails = String.join("; ", mismatches);
            }
        }

        Instant now = Instant.now();
        ReconciliationResult result = reconciliationResultRepository.findByTransactionId(transactionId)
                .orElse(null);
        if (result == null) {
            reconciliationResultRepository.save(
                    new ReconciliationResult(transactionId, status, sourcesSeen, discrepancyDetails, now));
        } else {
            result.update(status, sourcesSeen, discrepancyDetails, now);
        }

        if (status == ReconciliationStatus.DISCREPANCY) {
            discrepancyProducer.publish(new DiscrepancyEvent(transactionId, sourcesSeen, discrepancyDetails, now));
        }
    }

    private List<String> findMismatches(List<TransactionRecord> records) {
        TransactionRecord baseline = records.get(0);
        return records.stream()
                .skip(1)
                .flatMap(record -> compare(baseline, record).stream())
                .toList();
    }

    private List<String> compare(TransactionRecord baseline, TransactionRecord candidate) {
        List<String> mismatches = new java.util.ArrayList<>();

        if (baseline.getAmount().subtract(candidate.getAmount()).abs().compareTo(amountTolerance) > 0) {
            mismatches.add("amount mismatch between %s (%s) and %s (%s)".formatted(
                    baseline.getSourceSystem(), baseline.getAmount(),
                    candidate.getSourceSystem(), candidate.getAmount()));
        }
        if (!baseline.getCurrency().equals(candidate.getCurrency())) {
            mismatches.add("currency mismatch between %s (%s) and %s (%s)".formatted(
                    baseline.getSourceSystem(), baseline.getCurrency(),
                    candidate.getSourceSystem(), candidate.getCurrency()));
        }
        if (!baseline.getAccountId().equals(candidate.getAccountId())) {
            mismatches.add("account mismatch between %s (%s) and %s (%s)".formatted(
                    baseline.getSourceSystem(), baseline.getAccountId(),
                    candidate.getSourceSystem(), candidate.getAccountId()));
        }
        return mismatches;
    }
}
