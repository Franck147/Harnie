-- ============================================================
-- Harnie P2P Exchange — Supabase Schema
-- Divisas soportadas: PEN, USD, RUB
-- ============================================================

-- --------------------------------
-- TIPOS ENUMERADOS
-- --------------------------------

CREATE TYPE currency_code AS ENUM ('PEN', 'USD', 'RUB');
CREATE TYPE order_type AS ENUM ('BUY', 'SELL');
CREATE TYPE order_status AS ENUM ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'DISPUTED');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'CONFIRMED', 'REJECTED', 'EXPIRED');
CREATE TYPE payment_method_type AS ENUM ('BANK_TRANSFER', 'MOBILE_WALLET', 'CASH', 'CRYPTO_WALLET');
CREATE TYPE kyc_status AS ENUM ('NONE', 'PENDING', 'VERIFIED', 'REJECTED');

-- --------------------------------
-- PERFILES DE USUARIO
-- Extiende auth.users de Supabase
-- --------------------------------

CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    phone TEXT,
    kyc_status kyc_status NOT NULL DEFAULT 'NONE',
    preferred_currency currency_code NOT NULL DEFAULT 'USD',
    total_trades INTEGER NOT NULL DEFAULT 0,
    rating NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_profiles_kyc ON profiles(kyc_status);

-- --------------------------------
-- BALANCES MULTIDIVISA
-- Un registro por usuario por divisa
-- --------------------------------

CREATE TABLE balances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    currency currency_code NOT NULL,
    available NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    frozen NUMERIC(18, 4) NOT NULL DEFAULT 0.0000,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_balance_user_currency UNIQUE (user_id, currency),
    CONSTRAINT chk_available_non_negative CHECK (available >= 0),
    CONSTRAINT chk_frozen_non_negative CHECK (frozen >= 0)
);

CREATE INDEX idx_balances_user ON balances(user_id);

-- --------------------------------
-- MÉTODOS DE PAGO DEL USUARIO
-- --------------------------------

CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    method_type payment_method_type NOT NULL,
    label TEXT NOT NULL,
    currency currency_code NOT NULL,
    details JSONB NOT NULL DEFAULT '{}',
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_methods_user ON payment_methods(user_id);

-- --------------------------------
-- ÓRDENES P2P
-- El creador publica una orden de compra o venta
-- --------------------------------

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creator_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    order_type order_type NOT NULL,
    source_currency currency_code NOT NULL,
    target_currency currency_code NOT NULL,
    exchange_rate NUMERIC(18, 8) NOT NULL,
    amount NUMERIC(18, 4) NOT NULL,
    min_limit NUMERIC(18, 4) NOT NULL,
    max_limit NUMERIC(18, 4) NOT NULL,
    status order_status NOT NULL DEFAULT 'OPEN',
    payment_method_id UUID REFERENCES payment_methods(id) ON DELETE SET NULL,
    terms TEXT,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_rate_positive CHECK (exchange_rate > 0),
    CONSTRAINT chk_limits CHECK (min_limit > 0 AND max_limit >= min_limit AND max_limit <= amount),
    CONSTRAINT chk_different_currencies CHECK (source_currency <> target_currency)
);

CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_creator ON orders(creator_id);
CREATE INDEX idx_orders_currencies ON orders(source_currency, target_currency);
CREATE INDEX idx_orders_open_listing ON orders(status, source_currency, target_currency)
    WHERE status = 'OPEN';

-- --------------------------------
-- TRANSACCIONES (match entre dos usuarios)
-- --------------------------------

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    buyer_id UUID NOT NULL REFERENCES profiles(id),
    seller_id UUID NOT NULL REFERENCES profiles(id),
    amount NUMERIC(18, 4) NOT NULL,
    exchange_rate NUMERIC(18, 8) NOT NULL,
    source_currency currency_code NOT NULL,
    target_currency currency_code NOT NULL,
    status transaction_status NOT NULL DEFAULT 'PENDING',
    buyer_confirmed_at TIMESTAMPTZ,
    seller_confirmed_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (now() + INTERVAL '30 minutes'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_tx_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_tx_different_parties CHECK (buyer_id <> seller_id)
);

CREATE INDEX idx_transactions_order ON transactions(order_id);
CREATE INDEX idx_transactions_buyer ON transactions(buyer_id);
CREATE INDEX idx_transactions_seller ON transactions(seller_id);
CREATE INDEX idx_transactions_status ON transactions(status);

-- --------------------------------
-- VALORACIONES POST-TRANSACCIÓN
-- --------------------------------

CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    rater_id UUID NOT NULL REFERENCES profiles(id),
    rated_id UUID NOT NULL REFERENCES profiles(id),
    score SMALLINT NOT NULL,
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_rating_per_tx UNIQUE (transaction_id, rater_id),
    CONSTRAINT chk_score_range CHECK (score BETWEEN 1 AND 5),
    CONSTRAINT chk_different_rater CHECK (rater_id <> rated_id)
);

CREATE INDEX idx_ratings_rated ON ratings(rated_id);

-- --------------------------------
-- DISPUTAS
-- --------------------------------

CREATE TABLE disputes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    opened_by UUID NOT NULL REFERENCES profiles(id),
    reason TEXT NOT NULL,
    evidence_urls TEXT[] DEFAULT '{}',
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolution_note TEXT,
    resolved_by UUID REFERENCES profiles(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ
);

CREATE INDEX idx_disputes_transaction ON disputes(transaction_id);
CREATE INDEX idx_disputes_open ON disputes(resolved) WHERE resolved = FALSE;

-- --------------------------------
-- HISTORIAL DE MOVIMIENTOS DE BALANCE
-- Registro inmutable de cada cambio en balances
-- --------------------------------

CREATE TABLE balance_ledger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id),
    currency currency_code NOT NULL,
    delta NUMERIC(18, 4) NOT NULL,
    balance_after NUMERIC(18, 4) NOT NULL,
    reference_type TEXT NOT NULL,
    reference_id UUID NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ledger_user_currency ON balance_ledger(user_id, currency);
CREATE INDEX idx_ledger_created ON balance_ledger(created_at DESC);

-- --------------------------------
-- TIPOS DE CAMBIO DE REFERENCIA
-- Tasas del mercado para mostrar en el dashboard
-- --------------------------------

CREATE TABLE exchange_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_currency currency_code NOT NULL,
    target_currency currency_code NOT NULL,
    rate NUMERIC(18, 8) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_rate_pair UNIQUE (source_currency, target_currency),
    CONSTRAINT chk_rate_pair_different CHECK (source_currency <> target_currency)
);

-- --------------------------------
-- FUNCIONES Y TRIGGERS
-- --------------------------------

-- Actualiza updated_at automáticamente
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_profiles_updated
    BEFORE UPDATE ON profiles
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_balances_updated
    BEFORE UPDATE ON balances
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_orders_updated
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_transactions_updated
    BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER trg_exchange_rates_updated
    BEFORE UPDATE ON exchange_rates
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- Crea balances en las 3 divisas al registrar un nuevo perfil
CREATE OR REPLACE FUNCTION create_default_balances()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO balances (user_id, currency) VALUES
        (NEW.id, 'PEN'),
        (NEW.id, 'USD'),
        (NEW.id, 'RUB');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_profile_create_balances
    AFTER INSERT ON profiles
    FOR EACH ROW EXECUTE FUNCTION create_default_balances();

-- Actualiza el rating promedio y total_trades del perfil al recibir una valoración
CREATE OR REPLACE FUNCTION update_profile_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE profiles SET
        rating = (
            SELECT COALESCE(AVG(score), 0)
            FROM ratings
            WHERE rated_id = NEW.rated_id
        ),
        total_trades = (
            SELECT COUNT(DISTINCT transaction_id)
            FROM ratings
            WHERE rated_id = NEW.rated_id
        )
    WHERE id = NEW.rated_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_rating_update_profile
    AFTER INSERT ON ratings
    FOR EACH ROW EXECUTE FUNCTION update_profile_rating();

-- --------------------------------
-- ROW LEVEL SECURITY (RLS)
-- --------------------------------

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE balances ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_methods ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE ratings ENABLE ROW LEVEL SECURITY;
ALTER TABLE disputes ENABLE ROW LEVEL SECURITY;
ALTER TABLE balance_ledger ENABLE ROW LEVEL SECURITY;
ALTER TABLE exchange_rates ENABLE ROW LEVEL SECURITY;

-- Profiles: lectura pública, escritura propia
CREATE POLICY profiles_select ON profiles
    FOR SELECT USING (TRUE);

CREATE POLICY profiles_update ON profiles
    FOR UPDATE USING (auth.uid() = id);

-- Balances: solo el dueño ve y modifica sus balances
CREATE POLICY balances_select ON balances
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY balances_update ON balances
    FOR UPDATE USING (auth.uid() = user_id);

-- Payment methods: solo el dueño
CREATE POLICY payment_methods_all ON payment_methods
    FOR ALL USING (auth.uid() = user_id);

-- Orders: cualquiera ve las abiertas, solo el creador modifica
CREATE POLICY orders_select ON orders
    FOR SELECT USING (TRUE);

CREATE POLICY orders_insert ON orders
    FOR INSERT WITH CHECK (auth.uid() = creator_id);

CREATE POLICY orders_update ON orders
    FOR UPDATE USING (auth.uid() = creator_id);

-- Transactions: solo las partes involucradas
CREATE POLICY transactions_select ON transactions
    FOR SELECT USING (auth.uid() IN (buyer_id, seller_id));

CREATE POLICY transactions_insert ON transactions
    FOR INSERT WITH CHECK (auth.uid() IN (buyer_id, seller_id));

CREATE POLICY transactions_update ON transactions
    FOR UPDATE USING (auth.uid() IN (buyer_id, seller_id));

-- Ratings: lectura pública, escritura del valorador
CREATE POLICY ratings_select ON ratings
    FOR SELECT USING (TRUE);

CREATE POLICY ratings_insert ON ratings
    FOR INSERT WITH CHECK (auth.uid() = rater_id);

-- Disputes: solo las partes de la transacción
CREATE POLICY disputes_select ON disputes
    FOR SELECT USING (
        auth.uid() IN (
            SELECT buyer_id FROM transactions WHERE id = transaction_id
            UNION
            SELECT seller_id FROM transactions WHERE id = transaction_id
        )
    );

CREATE POLICY disputes_insert ON disputes
    FOR INSERT WITH CHECK (auth.uid() = opened_by);

-- Balance ledger: solo el dueño lee su historial
CREATE POLICY ledger_select ON balance_ledger
    FOR SELECT USING (auth.uid() = user_id);

-- Exchange rates: lectura pública
CREATE POLICY exchange_rates_select ON exchange_rates
    FOR SELECT USING (TRUE);

-- --------------------------------
-- HABILITAR REALTIME
-- Tablas que emitirán cambios via Supabase Realtime
-- --------------------------------

ALTER PUBLICATION supabase_realtime ADD TABLE orders;
ALTER PUBLICATION supabase_realtime ADD TABLE transactions;
ALTER PUBLICATION supabase_realtime ADD TABLE balances;
ALTER PUBLICATION supabase_realtime ADD TABLE exchange_rates;