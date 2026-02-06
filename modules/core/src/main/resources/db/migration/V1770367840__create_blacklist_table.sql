CREATE TABLE syneronix.blacklist
(
    id             UUID           NOT NULL,
    created_at     TIMESTAMP      NOT NULL,
    updated_at     TIMESTAMP,
    version        BIGINT         NOT NULL,
    wallet_id      UUID           NOT NULL,
    reason         VARCHAR(255)   NOT NULL,
    CONSTRAINT pk_blacklist PRIMARY KEY (id)
);

ALTER TABLE syneronix.blacklist
    OWNER TO wallet_db_user;

ALTER TABLE syneronix.blacklist
    ADD CONSTRAINT uc_blacklist_wallet_id UNIQUE (wallet_id);

ALTER TABLE syneronix.blacklist
    ADD CONSTRAINT FK_BLACKLIST_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES syneronix.wallets (id);

CREATE INDEX idx_blacklist_wallet_id ON syneronix.blacklist (wallet_id);
