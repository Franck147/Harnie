-- ============================================================
-- Harnie — Migration: Tabla de Clientes
-- ============================================================

CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    country country_code NOT NULL,
    phone TEXT,
    email TEXT,
    document_type TEXT,
    document_number TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_clients_user ON clients(user_id);
CREATE INDEX idx_clients_country ON clients(country);

-- RLS
ALTER TABLE clients ENABLE ROW LEVEL SECURITY;

CREATE POLICY clients_select ON clients
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY clients_insert ON clients
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY clients_update ON clients
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY clients_delete ON clients
    FOR DELETE USING (auth.uid() = user_id);
