CREATE TABLE IF NOT EXISTS syneronix.blacklist
(
    id             UUID           NOT NULL,
    created_at     TIMESTAMP      NOT NULL,
    updated_at     TIMESTAMP,
    version        BIGINT         NOT NULL,
    wallet_id      UUID           NOT NULL,
    reason         VARCHAR(255)   NOT NULL,
    CONSTRAINT pk_blacklist PRIMARY KEY (id),
    CONSTRAINT uc_blacklist_wallet_id UNIQUE (wallet_id),
    CONSTRAINT FK_BLACKLIST_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES syneronix.wallets (id)
);

ALTER TABLE syneronix.blacklist
    OWNER TO wallet_db_user;

CREATE INDEX IF NOT EXISTS idx_blacklist_wallet_id ON syneronix.blacklist (wallet_id);
