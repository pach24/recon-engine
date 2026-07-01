package com.recon.ingestion.web;

import com.recon.ingestion.kafka.TransactionProducer;
import com.recon.ingestion.model.Transaction;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class IngestionController {

    private final TransactionProducer transactionProducer;

    public IngestionController(TransactionProducer transactionProducer) {
        this.transactionProducer = transactionProducer;
    }

    @PostMapping
    public ResponseEntity<Void> ingest(@Valid @RequestBody TransactionRequest request) {
        Transaction transaction = new Transaction(
                request.transactionId(),
                request.sourceSystem(),
                request.accountId(),
                request.amount(),
                request.currency(),
                request.occurredAt()
        );
        transactionProducer.publish(transaction);
        return ResponseEntity.accepted().build();
    }
}
