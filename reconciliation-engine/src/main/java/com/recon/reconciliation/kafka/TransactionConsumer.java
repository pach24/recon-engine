package com.recon.reconciliation.kafka;

import com.recon.reconciliation.model.RawTransactionEvent;
import com.recon.reconciliation.persistence.TransactionRecord;
import com.recon.reconciliation.persistence.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    private final TransactionRepository transactionRepository;

    public TransactionConsumer(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @KafkaListener(topics = "${app.kafka.topic.raw-transactions}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(RawTransactionEvent event) {
        transactionRepository.findByTransactionIdAndSourceSystem(event.transactionId(), event.sourceSystem())
                .ifPresentOrElse(
                        existing -> log.info("Duplicate transaction event ignored: {} from {}",
                                event.transactionId(), event.sourceSystem()),
                        () -> transactionRepository.save(new TransactionRecord(
                                event.transactionId(),
                                event.sourceSystem(),
                                event.accountId(),
                                event.amount(),
                                event.currency(),
                                event.occurredAt(),
                                Instant.now()
                        ))
                );
    }
}
