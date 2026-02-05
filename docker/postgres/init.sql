CREATE DATABASE wallet_db OWNER wallet_db_user;
CREATE DATABASE wallet_db_test OWNER wallet_db_user;

\connect wallet_db
CREATE SCHEMA IF NOT EXISTS syneronix;

\connect wallet_db_test
CREATE SCHEMA IF NOT EXISTS syneronix;
