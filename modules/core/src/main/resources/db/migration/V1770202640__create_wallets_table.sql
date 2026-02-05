CREATE TABLE syneronix.wallets
(
    id         UUID           NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP,
    version    BIGINT         NOT NULL,
    user_id    UUID           NOT NULL,
    currency   VARCHAR(3)     NOT NULL,
    status     VARCHAR(8)     NOT NULL,
    balance    DECIMAL(19, 4) NOT NULL,

    CONSTRAINT pk_wallets PRIMARY KEY (id),
    CONSTRAINT chk_wallets_balance_positive CHECK (balance >= 0)
);

ALTER TABLE syneronix.wallets
    OWNER TO wallet_db_user;

CREATE INDEX idx_wallets_user_id ON syneronix.wallets (user_id);
