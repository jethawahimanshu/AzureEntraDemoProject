-- =============================================================
-- V1: Initial schema
-- Uses H2-compatible SQL (UUID type varies between H2 and PG)
-- For PostgreSQL production, replace VARCHAR(36) with UUID
-- and add gen_random_uuid() default on the products table.
-- =============================================================

CREATE TABLE users (
    oid          VARCHAR(36)   NOT NULL,
    display_name VARCHAR(255),
    email        VARCHAR(255),
    phone        VARCHAR(50),
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (oid)
);

CREATE INDEX idx_users_email ON users (email);

CREATE TABLE products (
    id             VARCHAR(36)    NOT NULL,
    name           VARCHAR(255)   NOT NULL,
    description    VARCHAR(2000),
    price          DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    category       VARCHAR(50),
    stock_quantity INT            DEFAULT 0,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

CREATE INDEX idx_products_category ON products (category);
