package com.recon.ingestion.legacy;

import com.recon.ingestion.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@Testcontainers
@SpringBootTest
class LegacyOraclePollerIntegrationTest {

    @Container
    static OracleContainer oracle = new OracleContainer(DockerImageName.parse("gvenzl/oracle-free:23-slim-faststart"));

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", oracle::getJdbcUrl);
        registry.add("spring.datasource.username", oracle::getUsername);
        registry.add("spring.datasource.password", oracle::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.legacy-oracle.poll-interval-ms", () -> "1000");
    }

    @Value("${app.kafka.topic.raw-transactions}")
    private String rawTransactionsTopic;

    @org.springframework.beans.factory.annotation.Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void publishesUnprocessedLegacyRowsToRawTransactionsTopic() {
        String transactionId = "TXN-" + UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO legacy_transactions (transaction_id, account_id, amount, currency, occurred_at, processed) " +
                        "VALUES (?, ?, ?, ?, SYSTIMESTAMP, 'N')",
                transactionId, "ACC-9", new BigDecimal("42.00"), "USD");

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                kafka.getBootstrapServers(), "test-group-" + UUID.randomUUID(), "true");
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, Transaction> consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new org.apache.kafka.common.serialization.StringDeserializer(),
                new JsonDeserializer<>(Transaction.class, false));

        try (var consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(java.util.List.of(rawTransactionsTopic));

            await().atMost(20, SECONDS).untilAsserted(() -> {
                var records = KafkaTestUtils.getRecords(consumer, java.time.Duration.ofSeconds(2));
                boolean found = false;
                for (var record : records.records(rawTransactionsTopic)) {
                    Transaction transaction = record.value();
                    if (transaction.transactionId().equals(transactionId)) {
                        assertThat(transaction.sourceSystem()).isEqualTo("LEGACY_ORACLE");
                        assertThat(transaction.amount()).isEqualByComparingTo("42.00");
                        found = true;
                    }
                }
                assertThat(found).isTrue();
            });
        }

        await().atMost(15, SECONDS).untilAsserted(() -> {
            String processed = jdbcTemplate.queryForObject(
                    "SELECT processed FROM legacy_transactions WHERE transaction_id = ?",
                    String.class, transactionId);
            assertThat(processed).isEqualTo("Y");
        });
    }
}
