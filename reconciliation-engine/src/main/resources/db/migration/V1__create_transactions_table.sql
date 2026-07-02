CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  VARCHAR(64)     NOT NULL,
    source_system   VARCHAR(64)     NOT NULL,
    account_id      VARCHAR(64)     NOT NULL,
    amount          NUMERIC(19, 4)  NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    occurred_at     TIMESTAMPTZ     NOT NULL,
    received_at     TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uq_transactions_txn_source UNIQUE (transaction_id, source_system)
);

CREATE INDEX idx_transactions_transaction_id ON transactions (transaction_id);
