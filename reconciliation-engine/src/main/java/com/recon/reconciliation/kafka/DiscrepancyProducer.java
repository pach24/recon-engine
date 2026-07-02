package com.recon.reconciliation.kafka;

import com.recon.reconciliation.model.DiscrepancyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DiscrepancyProducer {

    private final KafkaTemplate<String, DiscrepancyEvent> kafkaTemplate;
    private final String topic;

    public DiscrepancyProducer(KafkaTemplate<String, DiscrepancyEvent> kafkaTemplate,
                                @Value("${app.kafka.topic.discrepancies}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(DiscrepancyEvent event) {
        kafkaTemplate.send(topic, event.transactionId(), event);
    }
}
