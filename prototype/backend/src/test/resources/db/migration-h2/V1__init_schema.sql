CREATE TABLE program (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    base_value NUMERIC(12,2) NOT NULL,
    adjustment_factor NUMERIC(10,6) NOT NULL,
    fator_k NUMERIC(10,6) NOT NULL DEFAULT 0.347215,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_program_type CHECK (type IN ('ASSISTENCIAL', 'CONTRIBUTIVO'))
);

CREATE TABLE beneficiary (
    id UUID PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL,
    name VARCHAR(200) NOT NULL,
    birth_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    cod_region INTEGER NOT NULL,
    family_members INTEGER NOT NULL DEFAULT 0,
    family_income NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_beneficiary_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE', 'CANCELLED')),
    CONSTRAINT uq_beneficiary_cpf UNIQUE (cpf)
);

CREATE TABLE dependent (
    id UUID PRIMARY KEY,
    beneficiary_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    birth_date DATE NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dependent_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiary(id)
);

CREATE TABLE payment (
    id UUID PRIMARY KEY,
    beneficiary_id UUID NOT NULL,
    program_id UUID NOT NULL,
    reference_year_month VARCHAR(6) NOT NULL,
    gross_amount NUMERIC(12,2) NOT NULL,
    net_amount NUMERIC(12,2),
    corrected BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiary(id),
    CONSTRAINT fk_payment_program FOREIGN KEY (program_id) REFERENCES program(id),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'RECONCILED', 'DIVERGENT', 'CANCELLED')),
    CONSTRAINT uq_payment_beneficiary_month UNIQUE (beneficiary_id, reference_year_month)
);

CREATE TABLE payment_discount (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    CONSTRAINT fk_payment_discount_payment FOREIGN KEY (payment_id) REFERENCES payment(id)
);

CREATE TABLE eligibility_decision (
    id UUID PRIMARY KEY,
    beneficiary_id UUID NOT NULL,
    program_id UUID NOT NULL,
    decision VARCHAR(20) NOT NULL,
    reason VARCHAR(100) NOT NULL,
    decided_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eligibility_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiary(id),
    CONSTRAINT fk_eligibility_program FOREIGN KEY (program_id) REFERENCES program(id)
);

CREATE TABLE audit_event (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    previous_state CLOB,
    new_state CLOB,
    actor VARCHAR(200) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason CLOB
);

CREATE TABLE special_cpf_prefix (
    prefix VARCHAR(3) PRIMARY KEY,
    description CLOB,
    created_by VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);