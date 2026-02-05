CREATE TABLE syneronix.idempotency_keys
(
    id               UUID           NOT NULL,
    created_at       TIMESTAMP      NOT NULL,
    updated_at       TIMESTAMP,
    version          BIGINT         NOT NULL,
    request_id       UUID           NOT NULL,
    request_type     VARCHAR(100)   NOT NULL,
    currency         VARCHAR(3)     NOT NULL,
    from_wallet_id   UUID,
    to_wallet_id     UUID,
    status           VARCHAR(255)   NOT NULL,
    http_status_code INTEGER        NOT NULL,
    request_body     JSONB          NOT NULL,
    request_hash     VARCHAR(64),
    response_body    JSONB,
    fail_reason      VARCHAR(255),
    CONSTRAINT pk_idempotency_keys PRIMARY KEY (id)
);

ALTER TABLE syneronix.idempotency_keys
    OWNER TO wallet_db_user;

ALTER TABLE syneronix.idempotency_keys
    ADD CONSTRAINT uc_idempotency_keys_request_id UNIQUE (request_id);

CREATE INDEX idx_idempotency_keys_request_id ON syneronix.idempotency_keys (request_id);
