-- ============================================================
-- Harnie — Migration: Short ID + Custom Date Support
-- Agrega IDs cortos correlativos (H-0001) y permite
-- que el cliente envíe created_at personalizado.
-- ============================================================

-- 1. Nueva columna para el ID corto legible
ALTER TABLE orders ADD COLUMN IF NOT EXISTS short_id TEXT;

-- 2. Función que genera el siguiente correlativo H-XXXX por usuario
CREATE OR REPLACE FUNCTION generate_short_id()
RETURNS TRIGGER AS $$
DECLARE
    next_num INTEGER;
BEGIN
    -- Obtener el número máximo actual para este usuario
    SELECT COALESCE(
        MAX(
            CAST(
                SUBSTRING(short_id FROM 'H-([0-9]+)') AS INTEGER
            )
        ), 0
    ) + 1
    INTO next_num
    FROM orders
    WHERE creator_id = NEW.creator_id
      AND short_id IS NOT NULL;

    NEW.short_id := 'H-' || LPAD(next_num::TEXT, 4, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3. Trigger que se ejecuta al insertar una nueva orden
--    Solo asigna short_id si no viene uno ya definido.
DROP TRIGGER IF EXISTS trg_orders_short_id ON orders;
CREATE TRIGGER trg_orders_short_id
    BEFORE INSERT ON orders
    FOR EACH ROW
    WHEN (NEW.short_id IS NULL)
    EXECUTE FUNCTION generate_short_id();

-- 4. Backfill: asignar short_id a órdenes existentes que no lo tengan.
--    Se asignan en orden cronológico (created_at) por cada usuario.
DO $$
DECLARE
    r RECORD;
    current_user_id UUID := NULL;
    counter INTEGER := 0;
BEGIN
    FOR r IN
        SELECT id, creator_id
        FROM orders
        WHERE short_id IS NULL
        ORDER BY creator_id, created_at ASC
    LOOP
        IF r.creator_id IS DISTINCT FROM current_user_id THEN
            current_user_id := r.creator_id;
            -- Obtener el máximo actual de este usuario (por si tiene algunos ya)
            SELECT COALESCE(
                MAX(CAST(SUBSTRING(short_id FROM 'H-([0-9]+)') AS INTEGER)),
                0
            ) INTO counter
            FROM orders
            WHERE creator_id = current_user_id AND short_id IS NOT NULL;
        END IF;

        counter := counter + 1;

        UPDATE orders
        SET short_id = 'H-' || LPAD(counter::TEXT, 4, '0')
        WHERE id = r.id;
    END LOOP;
END $$;
