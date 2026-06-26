-- ============================================================
-- Harnie — Migration: Orders V2
-- Agrega campos para el nuevo flujo de creación de órdenes
-- ============================================================

-- Nuevos tipos enumerados
CREATE TYPE exchange_platform AS ENUM ('EL_DORADO', 'BYBIT');
CREATE TYPE country_code AS ENUM ('PERU', 'RUSSIA', 'ECUADOR');
CREATE TYPE document_type AS ENUM ('DNI', 'CARNET_EXTRANJERIA', 'PASAPORTE');

-- Nuevas columnas en orders
ALTER TABLE orders ADD COLUMN exchange exchange_platform;
ALTER TABLE orders ADD COLUMN country country_code;
ALTER TABLE orders ADD COLUMN payment_method TEXT;
ALTER TABLE orders ADD COLUMN fiat_amount NUMERIC(18, 4);
ALTER TABLE orders ADD COLUMN price_per_unit NUMERIC(18, 8);
ALTER TABLE orders ADD COLUMN usdt_amount NUMERIC(18, 4);
ALTER TABLE orders ADD COLUMN exchange_commission NUMERIC(18, 4);
ALTER TABLE orders ADD COLUMN document_type document_type;
ALTER TABLE orders ADD COLUMN document_number TEXT;
ALTER TABLE orders ADD COLUMN note TEXT;
