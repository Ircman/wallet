\connect wallet_db
GRANT ALL PRIVILEGES ON DATABASE wallet_db TO wallet_db_user;
CREATE SCHEMA IF NOT EXISTS syneronix;

CREATE DATABASE wallet_db_test;

GRANT ALL PRIVILEGES ON DATABASE wallet_db_test TO wallet_db_user;

CREATE SCHEMA IF NOT EXISTS syneronix;
