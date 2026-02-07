CREATE TABLE IF NOT EXISTS syneronix.ledger_entries
(
    id             UUID           NOT NULL,
    created_at     TIMESTAMP      NOT NULL,
    updated_at     TIMESTAMP,
    version        BIGINT         NOT NULL,
    transaction_id UUID           NOT NULL,
    wallet_id      UUID           NOT NULL,
    amount         DECIMAL(19, 4) NOT NULL,
    currency       VARCHAR(3)     NOT NULL,
    direction      VARCHAR(6)     NOT NULL,
    balance_after  DECIMAL(19, 4) NOT NULL,
    CONSTRAINT pk_ledger_entries PRIMARY KEY (id),
    CONSTRAINT chk_ledger_entries_amount_positive CHECK (amount > 0),
    CONSTRAINT FK_LEDGER_ENTRIES_ON_TRANSACTION FOREIGN KEY (transaction_id) REFERENCES syneronix.transactions (id),
    CONSTRAINT FK_LEDGER_ENTRIES_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES syneronix.wallets (id)
);

ALTER TABLE syneronix.ledger_entries
    OWNER TO wallet_db_user;

CREATE INDEX IF NOT EXISTS idx_ledger_entries_transaction_id ON syneronix.ledger_entries (transaction_id);
CREATE INDEX IF NOT EXISTS idx_ledger_entries_wallet_id ON syneronix.ledger_entries (wallet_id);
