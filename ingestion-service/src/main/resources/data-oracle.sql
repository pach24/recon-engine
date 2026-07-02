INSERT INTO legacy_transactions (transaction_id, account_id, amount, currency, occurred_at, processed)
SELECT 'TXN-LEGACY-SEED-1', 'ACC-1001', 250.00, 'USD', SYSTIMESTAMP, 'N' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM legacy_transactions WHERE transaction_id = 'TXN-LEGACY-SEED-1');

INSERT INTO legacy_transactions (transaction_id, account_id, amount, currency, occurred_at, processed)
SELECT 'TXN-LEGACY-SEED-2', 'ACC-1002', 89.50, 'USD', SYSTIMESTAMP, 'N' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM legacy_transactions WHERE transaction_id = 'TXN-LEGACY-SEED-2');
