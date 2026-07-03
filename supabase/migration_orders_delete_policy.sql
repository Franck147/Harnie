-- ============================================================
-- Harnie — Migration: Allow order deletion by creator
-- ============================================================
-- Sin esta politica, RLS bloquea el DELETE en silencio (borra 0 filas
-- sin lanzar error) y las ordenes parecen no eliminarse desde la app.
-- Ejecutar en Supabase -> SQL Editor.

DROP POLICY IF EXISTS orders_delete ON orders;

CREATE POLICY orders_delete ON orders
    FOR DELETE USING (auth.uid() = creator_id);

-- Verificacion: debe aparecer una fila con cmd = 'd'
-- SELECT polname, cmd FROM pg_policy WHERE polrelid = 'orders'::regclass;
