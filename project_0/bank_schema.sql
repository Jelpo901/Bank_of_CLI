CREATE TABLE IF NOT EXISTS account (
    account_id SERIAL PRIMARY KEY,
    pin VARCHAR(255) NOT NULL,
    balance NUMERIC(12, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS transaction (
    transaction_id SERIAL PRIMARY KEY,
    account_id INTEGER NOT NULL REFERENCES account(account_id),
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    related_account_id INTEGER REFERENCES account(account_id),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);