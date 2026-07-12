-- =============================================================================
-- V1__baseline.sql
-- Mevcut JPA entity'lerinden türetilmiş baseline şema (RBAC öncesi)
-- Kaynak entity'ler: User, Outlet, Category, Product (+ BaseEntity alanları)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- users
-- Entity: com.cavus.delivery_food.auth.entity.User
-- Not: role kolonu enum (USER, ADMIN) — RBAC geçişinde V2 migration ile taşınacak
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id          UUID            NOT NULL,
    email       VARCHAR(255)    NOT NULL,
    password    VARCHAR(100)    NOT NULL,
    first_name  VARCHAR(50)     NOT NULL,
    last_name   VARCHAR(50)     NOT NULL,
    role        VARCHAR(255)    NOT NULL,
    created_at  TIMESTAMP(6),
    updated_at  TIMESTAMP(6),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- -----------------------------------------------------------------------------
-- outlets
-- Entity: com.cavus.delivery_food.outlet.entity.Outlet
-- -----------------------------------------------------------------------------
CREATE TABLE outlets (
    id          UUID            NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    address     VARCHAR(500),
    phone       VARCHAR(15),
    email       VARCHAR(100),
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP(6),
    updated_at  TIMESTAMP(6),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT pk_outlets PRIMARY KEY (id),
    CONSTRAINT uk_outlets_name UNIQUE (name)
);

-- -----------------------------------------------------------------------------
-- categories
-- Entity: com.cavus.delivery_food.category.entity.Category
-- FK: outlet_id → outlets.id
-- -----------------------------------------------------------------------------
CREATE TABLE categories (
    id          UUID            NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(500),
    active      BOOLEAN         NOT NULL,
    outlet_id   UUID            NOT NULL,
    created_at  TIMESTAMP(6),
    updated_at  TIMESTAMP(6),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT fk_categories_outlet FOREIGN KEY (outlet_id) REFERENCES outlets (id)
);

CREATE INDEX idx_category_outlet ON categories (outlet_id);

-- -----------------------------------------------------------------------------
-- products
-- Entity: com.cavus.delivery_food.product.entity.Product
-- FK: category_id → categories.id (nullable), outlet_id → outlets.id
-- -----------------------------------------------------------------------------
CREATE TABLE products (
    id          UUID            NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    description VARCHAR(500),
    price       NUMERIC(10, 2)  NOT NULL,
    image_url   VARCHAR(255),
    stock       INTEGER         NOT NULL DEFAULT 0,
    unit        VARCHAR(20),
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    category_id UUID,
    outlet_id   UUID            NOT NULL,
    created_at  TIMESTAMP(6),
    updated_at  TIMESTAMP(6),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_products_outlet FOREIGN KEY (outlet_id) REFERENCES outlets (id)
);

CREATE INDEX idx_product_outlet ON products (outlet_id);
CREATE INDEX idx_product_category ON products (category_id);
