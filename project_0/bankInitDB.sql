CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGINT PRIMARY KEY,
    pin VARCHAR(4) NOT NULL,
    balance BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT check_balance_nonnegative CHECK (balance >= 0)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(account_id),
    related_account_id BIGINT REFERENCES accounts(account_id),
    transaction_type VARCHAR(10) NOT NULL,
    amount BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_amount_positive CHECK (amount > 0)
);