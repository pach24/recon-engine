package com.recon.ingestion.kafka;

import com.recon.ingestion.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionProducer {

    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final String topic;

    public TransactionProducer(KafkaTemplate<String, Transaction> kafkaTemplate,
                                @Value("${app.kafka.topic.raw-transactions}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(Transaction transaction) {
        kafkaTemplate.send(topic, transaction.transactionId(), transaction);
    }
}
