-- ============================================================
-- Harnie — Limpieza de datos de prueba
-- Vacía por completo ÓRDENES y CLIENTES (todos los usuarios).
--
-- ⚠️  IRREVERSIBLE. Ejecutar en el SQL Editor de Supabase.
--
-- TRUNCATE ... CASCADE también vacía las tablas que dependen de
-- 'orders' por clave foránea:
--   orders  -> transactions -> ratings, disputes
-- (en pruebas suelen estar vacías, pero se incluyen por el cascade).
--
-- NO toca: profiles, balances, payment_methods, exchange_rates.
-- ============================================================

TRUNCATE TABLE orders, clients RESTART IDENTITY CASCADE;

-- Verificación (deben dar 0):
SELECT 'orders'       AS tabla, COUNT(*) AS filas FROM orders
UNION ALL
SELECT 'clients'      AS tabla, COUNT(*) AS filas FROM clients
UNION ALL
SELECT 'transactions' AS tabla, COUNT(*) AS filas FROM transactions;
