package com.recon.ingestion.web;

import com.recon.ingestion.csv.CsvFormatException;
import com.recon.ingestion.csv.CsvTransactionParser;
import com.recon.ingestion.kafka.TransactionProducer;
import com.recon.ingestion.model.Transaction;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@RestController
@RequestMapping("/transactions")
public class IngestionController {

    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);

    private final TransactionProducer transactionProducer;
    private final CsvTransactionParser csvTransactionParser;

    public IngestionController(TransactionProducer transactionProducer,
                               CsvTransactionParser csvTransactionParser) {
        this.transactionProducer = transactionProducer;
        this.csvTransactionParser = csvTransactionParser;
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

    /**
     * Bulk ingestion of a CSV export. Valid rows are published to raw.transactions;
     * malformed rows are skipped and reported per-row so a single bad line does not
     * fail the whole upload.
     */
    @PostMapping(path = "/csv", consumes = "multipart/form-data")
    public ResponseEntity<CsvIngestionResponse> ingestCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new CsvFormatException("Uploaded file is empty");
        }

        CsvTransactionParser.Result result;
        try {
            result = csvTransactionParser.parse(file.getInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded CSV", e);
        }

        for (Transaction transaction : result.transactions()) {
            transactionProducer.publish(transaction);
        }

        log.info("CSV ingest: published {} transactions, {} rows rejected",
                result.transactions().size(), result.errors().size());

        return ResponseEntity.accepted().body(new CsvIngestionResponse(
                result.transactions().size() + result.errors().size(),
                result.transactions().size(),
                result.errors()));
    }

    @ExceptionHandler(CsvFormatException.class)
    public ResponseEntity<String> handleCsvFormat(CsvFormatException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
