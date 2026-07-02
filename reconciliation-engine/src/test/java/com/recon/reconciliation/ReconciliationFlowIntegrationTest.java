package com.recon.reconciliation;

import com.recon.reconciliation.model.RawTransactionEvent;
import com.recon.reconciliation.persistence.ReconciliationResultRepository;
import com.recon.reconciliation.persistence.ReconciliationStatus;
import com.recon.reconciliation.persistence.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@Testcontainers
@SpringBootTest
class ReconciliationFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReconciliationResultRepository reconciliationResultRepository;

    @Value("${app.kafka.topic.raw-transactions}")
    private String rawTransactionsTopic;

    @Test
    void matchesTransactionsWithinAmountTolerance() {
        String transactionId = "TXN-" + UUID.randomUUID();

        publish(transactionId, "BANKING", "ACC-1", new BigDecimal("100.00"));
        publish(transactionId, "ACCOUNTING", "ACC-1", new BigDecimal("100.00"));

        await().atMost(15, SECONDS).untilAsserted(() ->
                assertThat(reconciliationResultRepository.findByTransactionId(transactionId))
                        .hasValueSatisfying(result -> assertThat(result.getStatus())
                                .isEqualTo(ReconciliationStatus.MATCHED)));
    }

    @Test
    void flagsAmountMismatchAsDiscrepancy() {
        String transactionId = "TXN-" + UUID.randomUUID();

        publish(transactionId, "BANKING", "ACC-2", new BigDecimal("100.00"));
        publish(transactionId, "ACCOUNTING", "ACC-2", new BigDecimal("150.00"));

        await().atMost(15, SECONDS).untilAsserted(() ->
                assertThat(reconciliationResultRepository.findByTransactionId(transactionId))
                        .hasValueSatisfying(result -> {
                            assertThat(result.getStatus()).isEqualTo(ReconciliationStatus.DISCREPANCY);
                            assertThat(result.getDiscrepancyDetails()).contains("amount mismatch");
                        }));
    }

    @Test
    void staysPendingWithOnlyOneSource() {
        String transactionId = "TXN-" + UUID.randomUUID();

        publish(transactionId, "BANKING", "ACC-3", new BigDecimal("100.00"));

        await().atMost(15, SECONDS).untilAsserted(() ->
                assertThat(reconciliationResultRepository.findByTransactionId(transactionId))
                        .hasValueSatisfying(result -> assertThat(result.getStatus())
                                .isEqualTo(ReconciliationStatus.PENDING)));
    }

    @Test
    void ignoresDuplicateEventsFromTheSameSource() {
        String transactionId = "TXN-" + UUID.randomUUID();

        publish(transactionId, "BANKING", "ACC-4", new BigDecimal("100.00"));
        publish(transactionId, "BANKING", "ACC-4", new BigDecimal("100.00"));

        await().atMost(15, SECONDS).untilAsserted(() ->
                assertThat(reconciliationResultRepository.findByTransactionId(transactionId)).isPresent());

        assertThat(transactionRepository.findByTransactionId(transactionId)).hasSize(1);
    }

    private void publish(String transactionId, String sourceSystem, String accountId, BigDecimal amount) {
        RawTransactionEvent event = new RawTransactionEvent(
                transactionId, sourceSystem, accountId, amount, "USD", Instant.now());
        kafkaTemplate.send(rawTransactionsTopic, transactionId, event);
    }
}
