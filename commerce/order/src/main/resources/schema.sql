CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE IF NOT EXISTS orders.orders (
    order_id          UUID PRIMARY KEY,
    cart_id           UUID NOT NULL,
    user_name         VARCHAR(255),
    payment_id        UUID,
    delivery_id       UUID,
    order_state       VARCHAR(20),
    delivery_weight   DOUBLE PRECISION,
    delivery_volume   DOUBLE PRECISION,
    fragile           BOOLEAN,
    total_price       DECIMAL,
    delivery_price    DECIMAL,
    product_price     DECIMAL
);

CREATE TABLE IF NOT EXISTS orders.order_items (
    id            UUID PRIMARY KEY,
    order_id      UUID NOT NULL REFERENCES orders.orders(order_id) ON DELETE CASCADE,
    product_id    UUID NOT NULL,
    quantity      INTEGER NOT NULL
);