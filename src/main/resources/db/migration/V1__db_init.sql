CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(50)
);
CREATE SEQUENCE IF NOT EXISTS USER_SEQ START WITH 5 INCREMENT BY 1;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    TCKN VARCHAR(15) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);
-- V1__create_sequences.sql
CREATE SEQUENCE IF NOT EXISTS wallet_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE wallet (
    id BIGINT DEFAULT NEXT VALUE FOR wallet_seq PRIMARY KEY,
    iban VARCHAR(34) NOT NULL,
    name VARCHAR(50) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    active_for_shopping BOOLEAN NOT NULL DEFAULT TRUE,
    active_for_withdraw BOOLEAN NOT NULL DEFAULT TRUE,
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0,
    usable_balance DECIMAL(19, 4) NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL,
    CONSTRAINT uc_wallet_iban UNIQUE (iban),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users (id)
);




CREATE TABLE user_role (
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, user_id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE SEQUENCE IF NOT EXISTS transaction_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL NOT NULL,
    description VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    reference_number VARCHAR(36) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    from_wallet_id BIGINT NOT NULL,
    to_wallet_id BIGINT NOT NULL,
    type_id BIGINT NOT NULL,
    FOREIGN KEY (from_wallet_id) REFERENCES wallet(id),
    FOREIGN KEY (to_wallet_id) REFERENCES wallet(id),
    FOREIGN KEY (type_id) REFERENCES type(id)
);

CREATE UNIQUE INDEX wallet_user_id_iban_key ON wallet (user_id, iban);
CREATE UNIQUE INDEX wallet_user_id_name_key ON wallet (user_id, name);