-- V1__init_ledger_schema.sql

CREATE TABLE accounts (
    id           UUID PRIMARY KEY,
    owner_ref    VARCHAR(255) NOT NULL,
    currency     VARCHAR(3)   NOT NULL,
    type         VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_accounts_owner_ref UNIQUE (owner_ref)
);

CREATE TABLE transactions (
    id                UUID PRIMARY KEY,
    idempotency_key   VARCHAR(255) NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_transactions_idempotency_key UNIQUE (idempotency_key)
);

CREATE TABLE ledger_entries (
    id               UUID PRIMARY KEY,
    transaction_id   UUID NOT NULL REFERENCES transactions(id),
    account_id       UUID NOT NULL REFERENCES accounts(id),
    type             VARCHAR(10)     NOT NULL,
    amount           NUMERIC(19,4)   NOT NULL CHECK (amount > 0),
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT chk_ledger_entries_type CHECK (type IN ('DEBIT', 'CREDIT'))
);

-- Every balance lookup will filter by account_id — this index makes that fast
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);

-- Every entry lookup for a transaction (e.g. verifying it nets to zero) needs this
CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);