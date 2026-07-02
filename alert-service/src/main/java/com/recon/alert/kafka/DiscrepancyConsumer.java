package com.recon.alert.kafka;

import com.recon.alert.alerting.AlertStore;
import com.recon.alert.model.Alert;
import com.recon.alert.model.DiscrepancyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DiscrepancyConsumer {

    private static final Logger log = LoggerFactory.getLogger(DiscrepancyConsumer.class);

    private final AlertStore alertStore;

    public DiscrepancyConsumer(AlertStore alertStore) {
        this.alertStore = alertStore;
    }

    @KafkaListener(topics = "${app.kafka.topic.discrepancies}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(DiscrepancyEvent event) {
        log.warn("Discrepancy detected for transaction {}: {}", event.transactionId(), event.discrepancyDetails());
        alertStore.save(Alert.from(event, Instant.now()));
    }
}
