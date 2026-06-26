-- ============================================================
-- Harnie — Migration: Client Fields en Orders
-- ============================================================

ALTER TABLE orders ADD COLUMN client_phone TEXT;
ALTER TABLE orders ADD COLUMN client_email TEXT;
ALTER TABLE orders ADD COLUMN client_id UUID REFERENCES clients(id) ON DELETE SET NULL;
ALTER TABLE orders ADD COLUMN client_name TEXT;
ALTER TABLE orders ADD COLUMN client_last_name TEXT;

CREATE INDEX idx_orders_client ON orders(client_id);
