CREATE TABLE legacy_transactions (
    transaction_id  VARCHAR2(64) NOT NULL,
    account_id      VARCHAR2(64) NOT NULL,
    amount          NUMBER(18,2) NOT NULL,
    currency        VARCHAR2(8)  NOT NULL,
    occurred_at     TIMESTAMP    NOT NULL,
    processed       CHAR(1)      DEFAULT 'N' NOT NULL,
    CONSTRAINT pk_legacy_transactions PRIMARY KEY (transaction_id)
);
