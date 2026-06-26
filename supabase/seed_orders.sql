-- ============================================================
-- Harnie — Seed: Ordenes ficticias
-- Requiere un usuario existente. Reemplaza el UUID de abajo
-- con el auth.uid() de tu usuario en Supabase.
-- ============================================================

-- Reemplaza este UUID con tu creator_id real
DO $$
DECLARE
    uid UUID := 'REEMPLAZA-CON-TU-USER-ID';
BEGIN

-- ── Peru — Compras ──

INSERT INTO orders (creator_id, order_type, source_currency, target_currency, exchange_rate, amount, min_limit, max_limit, status, exchange, country, payment_method, fiat_amount, price_per_unit, usdt_amount, exchange_commission, note, created_at)
VALUES
(uid, 'BUY', 'PEN', 'USD', 3.7200, 1500.00, 1500.00, 1500.00, 'OPEN', 'EL_DORADO', 'PERU', 'Yape', 1500.00, 3.7200, 403.23, 2.50, 'Compra rapida por Yape', now() - interval '1 hour'),
(uid, 'BUY', 'PEN', 'USD', 3.7150, 2000.00, 2000.00, 2000.00, 'OPEN', 'BYBIT', 'PERU', 'BCP', 2000.00, 3.7150, 538.36, 3.00, null, now() - interval '3 hours'),
(uid, 'BUY', 'PEN', 'USD', 3.7300, 800.00, 800.00, 800.00, 'OPEN', 'EL_DORADO', 'PERU', 'Plin', 800.00, 3.7300, 214.48, null, null, now() - interval '1 day'),
(uid, 'BUY', 'USD', 'PEN', 1.0000, 500.00, 500.00, 500.00, 'OPEN', 'EL_DORADO', 'PERU', 'Transferencia', 500.00, 1.0000, 500.00, 1.50, 'Dolares via transferencia', now() - interval '1 day 2 hours');

-- ── Peru — Ventas ──

INSERT INTO orders (creator_id, order_type, source_currency, target_currency, exchange_rate, amount, min_limit, max_limit, status, exchange, country, payment_method, fiat_amount, price_per_unit, usdt_amount, exchange_commission, note, created_at)
VALUES
(uid, 'SELL', 'PEN', 'USD', 3.7000, 3000.00, 3000.00, 3000.00, 'OPEN', 'EL_DORADO', 'PERU', 'Interbank', 3000.00, 3.7000, 810.81, 4.00, 'Venta grande Interbank', now() - interval '2 hours'),
(uid, 'SELL', 'PEN', 'USD', 3.7100, 1200.00, 1200.00, 1200.00, 'OPEN', 'BYBIT', 'PERU', 'BBVA', 1200.00, 3.7100, 323.45, null, null, now() - interval '5 hours'),
(uid, 'SELL', 'PEN', 'USD', 3.7250, 600.00, 600.00, 600.00, 'OPEN', 'EL_DORADO', 'PERU', 'Yape', 600.00, 3.7250, 161.07, 1.00, null, now() - interval '2 days');

-- ── Ecuador — Compras ──

INSERT INTO orders (creator_id, order_type, source_currency, target_currency, exchange_rate, amount, min_limit, max_limit, status, exchange, country, payment_method, fiat_amount, price_per_unit, usdt_amount, exchange_commission, note, created_at)
VALUES
(uid, 'BUY', 'USD', 'PEN', 1.0000, 300.00, 300.00, 300.00, 'OPEN', 'EL_DORADO', 'ECUADOR', 'Banco Pichincha', 300.00, 1.0000, 300.00, 2.00, 'Compra desde Ecuador', now() - interval '4 hours'),
(uid, 'BUY', 'USD', 'PEN', 1.0000, 750.00, 750.00, 750.00, 'OPEN', 'BYBIT', 'ECUADOR', 'Banco Guayaquil', 750.00, 1.0000, 750.00, 3.50, null, now() - interval '6 hours'),
(uid, 'BUY', 'USD', 'PEN', 1.0000, 150.00, 150.00, 150.00, 'OPEN', 'EL_DORADO', 'ECUADOR', 'Transferencia de otros bancos', 150.00, 1.0000, 150.00, null, 'Monto pequeno', now() - interval '1 day 5 hours');

-- ── Ecuador — Ventas ──

INSERT INTO orders (creator_id, order_type, source_currency, target_currency, exchange_rate, amount, min_limit, max_limit, status, exchange, country, payment_method, fiat_amount, price_per_unit, usdt_amount, exchange_commission, note, created_at)
VALUES
(uid, 'SELL', 'USD', 'PEN', 1.0000, 500.00, 500.00, 500.00, 'OPEN', 'EL_DORADO', 'ECUADOR', 'Banco Pichincha', 500.00, 1.0000, 500.00, 2.50, null, now() - interval '30 minutes'),
(uid, 'SELL', 'USD', 'PEN', 1.0000, 1000.00, 1000.00, 1000.00, 'OPEN', 'BYBIT', 'ECUADOR', 'Banco Guayaquil', 1000.00, 1.0000, 1000.00, 5.00, 'Venta urgente Ecuador', now() - interval '8 hours');

-- ── Rusia — Compras ──

INSERT INTO orders (creator_id, order_type, source_currency, target_currency, exchange_rate, amount, min_limit, max_limit, status, exchange, country, payment_method, fiat_amount, price_per_unit, usdt_amount, exchange_commission, note, created_at)
VALUES
(uid, 'BUY', 'RUB', 'USD', 92.5000, 50000.00, 50000.00, 50000.00, 'OPEN', 'BYBIT', 'RUSSIA', 'Transferencia', 50000.00, 92.5000, 540.54, 5.00, 'Compra rublos Bybit', now() - interval '7 hours'),
(uid, 'BUY', 'RUB', 'USD', 93.0000, 25000.00, 25000.00, 25000.00, 'OPEN', 'EL_DORADO', 'RUSSIA', 'Transferencia', 25000.00, 93.0000, 268.82, 3.00, null, now() - interval '1 day 8 hours');

-- ── Rusia — Ventas ──

INSERT INTO orders (creator_id, order_type, source_currency, target_currency, exchange_rate, amount, min_limit, max_limit, status, exchange, country, payment_method, fiat_amount, price_per_unit, usdt_amount, exchange_commission, note, created_at)
VALUES
(uid, 'SELL', 'RUB', 'USD', 91.8000, 75000.00, 75000.00, 75000.00, 'OPEN', 'BYBIT', 'RUSSIA', 'Transferencia', 75000.00, 91.8000, 816.99, 8.00, 'Venta grande rublos', now() - interval '45 minutes'),
(uid, 'SELL', 'RUB', 'USD', 92.0000, 30000.00, 30000.00, 30000.00, 'OPEN', 'EL_DORADO', 'RUSSIA', 'Transferencia', 30000.00, 92.0000, 326.09, null, null, now() - interval '3 days');

END $$;
