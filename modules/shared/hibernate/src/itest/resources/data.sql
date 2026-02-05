CREATE SCHEMA IF NOT EXISTS memory_db;

CREATE TABLE IF NOT EXISTS memory_db.test
(
    id   UUID NOT NULL PRIMARY KEY,
    test VARCHAR(40)
);

CREATE UNIQUE INDEX circle_created_date_index
    ON memory_db.test (id DESC);
