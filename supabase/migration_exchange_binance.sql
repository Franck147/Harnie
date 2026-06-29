-- ============================================================
-- Migración: agregar 'BINANCE' al enum exchange_platform
-- ============================================================
-- El tipo exchange_platform se creó en migration_orders_v2.sql con
-- los valores ('EL_DORADO', 'BYBIT'). Para poder registrar órdenes
-- con el exchange Binance se añade el nuevo valor al enum.
--
-- Nota: ADD VALUE IF NOT EXISTS es idempotente; se puede ejecutar
-- varias veces sin error.

ALTER TYPE exchange_platform ADD VALUE IF NOT EXISTS 'BINANCE';
