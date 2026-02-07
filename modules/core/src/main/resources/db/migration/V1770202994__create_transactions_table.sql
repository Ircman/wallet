CREATE TABLE IF NOT EXISTS syneronix.transactions
(
    id             UUID           NOT NULL,
    created_at     TIMESTAMP      NOT NULL,
    updated_at     TIMESTAMP,
    version        BIGINT         NOT NULL,
    request_id     UUID           NOT NULL,
    type           VARCHAR(20)    NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    amount         DECIMAL(19, 4) NOT NULL,
    currency       VARCHAR(3)     NOT NULL,
    from_wallet_id UUID,
    to_wallet_id   UUID,
    description    VARCHAR(1024),
    failure_reason VARCHAR(1024),
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT chk_transactions_amount_positive CHECK (amount > 0),
    CONSTRAINT uc_transactions_request_id UNIQUE (request_id),
    CONSTRAINT FK_TRANSACTIONS_ON_FROM_WALLET FOREIGN KEY (from_wallet_id) REFERENCES syneronix.wallets (id),
    CONSTRAINT FK_TRANSACTIONS_ON_TO_WALLET FOREIGN KEY (to_wallet_id) REFERENCES syneronix.wallets (id)
);

ALTER TABLE syneronix.transactions
    OWNER TO wallet_db_user;

CREATE INDEX IF NOT EXISTS idx_transactions_from_wallet ON syneronix.transactions (from_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transactions_to_wallet ON syneronix.transactions (to_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transactions_request_id ON syneronix.transactions (request_id);
