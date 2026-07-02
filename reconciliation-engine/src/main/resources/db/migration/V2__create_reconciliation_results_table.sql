CREATE TABLE reconciliation_results (
    id                   BIGSERIAL PRIMARY KEY,
    transaction_id       VARCHAR(64)  NOT NULL,
    status               VARCHAR(16)  NOT NULL,
    sources_seen         VARCHAR(255) NOT NULL,
    discrepancy_details  TEXT,
    updated_at           TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_reconciliation_results_txn UNIQUE (transaction_id)
);
