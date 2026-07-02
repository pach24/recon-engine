package com.recon.reconciliation.web;

import com.recon.reconciliation.persistence.ReconciliationResultRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reconciliation")
public class ReconciliationController {

    private final ReconciliationResultRepository reconciliationResultRepository;

    public ReconciliationController(ReconciliationResultRepository reconciliationResultRepository) {
        this.reconciliationResultRepository = reconciliationResultRepository;
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ReconciliationResultResponse> getResult(@PathVariable("transactionId") String transactionId) {
        return reconciliationResultRepository.findByTransactionId(transactionId)
                .map(ReconciliationResultResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
