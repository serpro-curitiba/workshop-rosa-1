CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sifap_app') THEN
        CREATE ROLE sifap_app;
    END IF;
END
$$;

CREATE TYPE beneficiary_status AS ENUM ('ACTIVE', 'SUSPENDED', 'INACTIVE', 'CANCELLED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'RECONCILED', 'DIVERGENT', 'CANCELLED');

CREATE TABLE program (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    base_value NUMERIC(12,2) NOT NULL,
    adjustment_factor NUMERIC(10,6) NOT NULL,
    fator_k NUMERIC(10,6) NOT NULL DEFAULT 0.347215,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_program_type CHECK (type IN ('ASSISTENCIAL', 'CONTRIBUTIVO'))
);

CREATE TABLE beneficiary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cpf VARCHAR(11) NOT NULL,
    name VARCHAR(200) NOT NULL,
    birth_date DATE NOT NULL,
    status beneficiary_status NOT NULL DEFAULT 'ACTIVE',
    cod_region INTEGER NOT NULL,
    family_members INTEGER NOT NULL DEFAULT 0,
    family_income NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_beneficiary_cpf UNIQUE (cpf)
);

CREATE TABLE dependent (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    beneficiary_id UUID NOT NULL REFERENCES beneficiary(id),
    name VARCHAR(200) NOT NULL,
    birth_date DATE NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    beneficiary_id UUID NOT NULL REFERENCES beneficiary(id),
    program_id UUID NOT NULL REFERENCES program(id),
    reference_year_month VARCHAR(6) NOT NULL,
    gross_amount NUMERIC(12,2) NOT NULL,
    net_amount NUMERIC(12,2),
    corrected BOOLEAN NOT NULL DEFAULT FALSE,
    status payment_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_payment_beneficiary_month UNIQUE (beneficiary_id, reference_year_month)
);

CREATE TABLE payment_discount (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payment(id),
    type VARCHAR(50) NOT NULL,
    amount NUMERIC(12,2) NOT NULL
);

CREATE TABLE eligibility_decision (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    beneficiary_id UUID NOT NULL REFERENCES beneficiary(id),
    program_id UUID NOT NULL REFERENCES program(id),
    decision VARCHAR(20) NOT NULL,
    reason VARCHAR(100) NOT NULL,
    decided_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE audit_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    previous_state JSONB,
    new_state JSONB,
    actor VARCHAR(200) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    reason TEXT
);

CREATE TABLE special_cpf_prefix (
    prefix VARCHAR(3) PRIMARY KEY,
    description TEXT,
    created_by VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

REVOKE UPDATE, DELETE ON audit_event FROM sifap_app;