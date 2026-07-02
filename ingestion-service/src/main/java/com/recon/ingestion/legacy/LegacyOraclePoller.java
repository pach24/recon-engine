package com.recon.ingestion.legacy;

import com.recon.ingestion.kafka.TransactionProducer;
import com.recon.ingestion.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Simulates a legacy batch export: polls unprocessed rows from an Oracle table
 * and republishes them onto the same raw.transactions topic used by the JSON API,
 * so the reconciliation-engine treats Oracle as a genuine second source.
 */
@Component
public class LegacyOraclePoller {

    private static final Logger log = LoggerFactory.getLogger(LegacyOraclePoller.class);

    private final JdbcTemplate jdbcTemplate;
    private final TransactionProducer transactionProducer;
    private final String sourceSystem;

    public LegacyOraclePoller(JdbcTemplate jdbcTemplate,
                               TransactionProducer transactionProducer,
                               @Value("${app.legacy-oracle.source-system}") String sourceSystem) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionProducer = transactionProducer;
        this.sourceSystem = sourceSystem;
    }

    @Scheduled(fixedDelayString = "${app.legacy-oracle.poll-interval-ms}")
    public void pollAndPublish() {
        List<LegacyRow> rows = jdbcTemplate.query(
                "SELECT transaction_id, account_id, amount, currency, occurred_at " +
                        "FROM legacy_transactions WHERE processed = 'N'",
                (rs, rowNum) -> new LegacyRow(
                        rs.getString("transaction_id"),
                        rs.getString("account_id"),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency"),
                        rs.getTimestamp("occurred_at").toInstant()
                ));

        for (LegacyRow row : rows) {
            transactionProducer.publish(new Transaction(
                    row.transactionId(),
                    sourceSystem,
                    row.accountId(),
                    row.amount(),
                    row.currency(),
                    row.occurredAt()
            ));

            jdbcTemplate.update(
                    "UPDATE legacy_transactions SET processed = 'Y' WHERE transaction_id = ?",
                    row.transactionId());

            log.info("Published legacy Oracle transaction {} to raw.transactions", row.transactionId());
        }
    }

    private record LegacyRow(String transactionId, String accountId, BigDecimal amount,
                              String currency, Instant occurredAt) {
    }
}
