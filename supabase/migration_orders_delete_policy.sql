-- ============================================================
-- Harnie — Migration: Allow order deletion by creator
-- ============================================================

CREATE POLICY orders_delete ON orders
    FOR DELETE USING (auth.uid() = creator_id);
