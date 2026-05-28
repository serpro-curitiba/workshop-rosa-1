# Feature: SIFAP 2.0 — Modernização do Sistema de Pagamentos

> Gerado por: @architect via /speckit.specify
> Data: 27/05/2026
> Fonte: 02-spec-moderna/SPECIFICATION.md · ADR-001 a ADR-005 · scope-decisions.md
> Estágio: 2 — Spec Moderna → input para Estágio 3

---

## Overview

Modernizar o SIFAP legado (Natural/Adabas) para Java 21 + Spring Boot 3.3 + PostgreSQL 16 + Next.js 15, preservando fidelidade às 18 regras de negócio catalogadas no Estágio 1 e resolvendo os 5 mistérios críticos (MYS-001/002/003/005/010).

**Arquitetura-alvo:** Modular Monolith — um único JAR implantável com 5 bounded contexts internos. Decisão documentada em ADR-001.

**Estratégia de migração:** Strangler Fig incremental — módulo `payment` primeiro, depois `beneficiary`, `eligibility`, `program`, `audit`. Decisão documentada em ADR-002.

---

## Bounded Contexts (recorte @architect)

| Módulo | Agregados principais | Programas legado de origem |
|--------|---------------------|---------------------------|
| `payment` | PaymentEntity, DiscountEntity, ReconciliationEntity | CALCBENF, CALCDSCT, CALCCORR, BATCHPGT, BATCHCON |
| `beneficiary` | BeneficiaryEntity, DependentEntity, DocumentValidationPolicy | CADBENEF, CADDEPEND, VALBENEF, VALDOCS, CONSBENF |
| `eligibility` | EligibilityEntity, EligibilityRule | VALELEG, VALBENEF |
| `program` | ProgramEntity | CADPROG |
| `audit` | AuditEvent (append-only) | RELAUDIT, BATCHCON (trilha) |

---

## Requirements

### Módulo `payment`

```yaml
REQ-PAY-001:
  pattern: ubiquitous
  text: "O SIFAP deve calcular o valor bruto do benefício mensal usando a fórmula
         multiparamétrica truncada para 2 casas decimais."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L228-L239
  priority: P0

REQ-PAY-002:
  pattern: complex
  text: "Enquanto mês = dezembro e programa tipo 'A', o SIFAP deve adicionar
         13º (1 benefício) e abono natalino de 15% ao valor bruto."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L248-L265
  priority: P0

REQ-PAY-003:
  pattern: state-driven
  text: "Enquanto IND-CORRIGIDO != 'S' e diferença positiva, o SIFAP deve aplicar
         correção retroativa e marcar IND-CORRIGIDO = 'S'."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L140-L166
  priority: P1

REQ-PAY-004:
  pattern: unwanted
  text: "O SIFAP não deve permitir que descontos não judiciais excedam 30% do bruto."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L102-L169
  priority: P0

REQ-PAY-005:
  pattern: event-driven
  text: "Quando desconto tipo JUDICIAL, o SIFAP deve adicioná-lo sem aplicar teto."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L102-L169
  priority: P0

REQ-PAY-006:
  pattern: unwanted
  text: "O SIFAP não deve gerar pagamento duplicado para mesma competência."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L195-L210
  priority: P0

REQ-PAY-007:
  pattern: event-driven
  text: "Quando bruto > R$ 500 em batch, o SIFAP deve aplicar desconto simplificado de 3%."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L308-L312
  priority: P1

REQ-PAY-008:
  pattern: event-driven
  text: "Quando arquivo de retorno bancário processado, o SIFAP deve marcar DIVERGENTE
         se diferença > R$ 0,01 e gravar evento de auditoria."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L155-L202
  priority: P0
```

### Módulo `beneficiary`

```yaml
REQ-BEN-001:
  pattern: event-driven
  text: "Quando beneficiário cadastrado/alterado, o SIFAP deve validar CPF por módulo 11."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L99-L117
  priority: P0

REQ-BEN-002:
  pattern: event-driven
  text: "Quando beneficiário tem idade > 75 anos, o SIFAP deve definir status como SUSPENDED."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L167-L169
  priority: P0

REQ-BEN-003:
  pattern: unwanted
  text: "O SIFAP não deve permitir dependente para beneficiário CANCELLED/INACTIVE
         ou com 5 dependentes já cadastrados."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L56-L66
  priority: P0

REQ-BEN-004:
  pattern: unwanted
  text: "O SIFAP não deve aceitar CPF com todos dígitos iguais como válido
         (exceção legada do prefixo 000 eliminada)."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L195-L200
  priority: P0

REQ-BEN-005:
  pattern: where
  text: "Onde CPF tiver prefixo na tabela de prefixos especiais, o SIFAP deve
         aceitar sem validação módulo 11."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L174-L180
  priority: P1

REQ-HIST-001:
  pattern: event-driven
  text: "Quando histórico de pagamentos solicitado para um beneficiário, o SIFAP
         deve retornar os últimos 12 pagamentos ordenados por competência decrescente."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN
  priority: P1
```

### Módulo `program`

```yaml
REQ-PROG-001:
  pattern: ubiquitous
  text: "O SIFAP deve calcular valor base ajustado com FATOR_K externalizável
         (padrão 0.347215)."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L87-L88
  priority: P0

REQ-PROG-002:
  pattern: ubiquitous
  text: "O SIFAP deve usar truncamento RoundingMode.DOWN em 2 casas decimais
         em todos os cálculos financeiros."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L117-L133
  priority: P0
```

### Módulo `eligibility`

```yaml
REQ-ELEG-001:
  pattern: event-driven
  text: "Quando COD-REGIAO = 99, o SIFAP deve aprovar elegibilidade imediatamente
         com motivo 'REGIAO_ESPECIAL_99'."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L107-L111
  priority: P0

REQ-ELEG-002:
  pattern: complex
  text: "Enquanto programa ASSISTENCIAL, quando renda > R$ 600 e sem dependentes,
         o SIFAP deve reprovar elegibilidade."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L168-L182
  priority: P0
```

### Módulo `audit`

```yaml
REQ-AUD-001:
  pattern: unwanted
  text: "O SIFAP não deve permitir DELETE ou UPDATE na tabela de auditoria."
  source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm#L9-L12
  priority: P0

REQ-AUD-002:
  pattern: ubiquitous
  text: "O SIFAP deve exibir todos os eventos de auditoria incluindo EXCLUSAO,
         sem filtros fixos."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L105-L108
  priority: P0

REQ-AUD-003:
  pattern: event-driven
  text: "Quando status de entidade rastreada alterado, o SIFAP deve gravar evento
         de auditoria com 6 campos obrigatórios."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L45-L72
  priority: P0
```

### NFRs / Greenfield

```yaml
REQ-SEC-001:
  pattern: ubiquitous
  text: "O SIFAP deve mascarar CPF em logs no formato XXX.XXX.NNN-NN."
  source_legacy: "[GREENFIELD] LGPD Art. 6º — MYS-007 indicou risco em CONSBENF.NSN#L177-L188"
  priority: P0

REQ-STATUS-001:
  pattern: ubiquitous
  text: "O SIFAP deve usar status canônico: PENDING, APPROVED, REJECTED,
         RECONCILED, DIVERGENT, CANCELLED em toda a codebase."
  source_legacy: "[GREENFIELD] MYS-010: divergência X vs C entre DDM e relatórios"
  priority: P0
```

---

## Acceptance Criteria (resumo por módulo)

### payment
- Fórmula BR-005: truncamento em 2 casas, sem arredondamento
- 13º dezembro: só para programa tipo 'A'
- Desconto teto: 35% TAX → truncado a 30%; JUDICIAL 80% → integral
- Antiduplicidade: segundo batch mesmo mês não cria novos pagamentos
- Conciliação: diferença R$ 0,02 → DIVERGENTE; R$ 0,01 → CONFIRMADO

### beneficiary
- CPF inválido → HTTP 400; CPF 000...000 → HTTP 400 (sem exceção)
- Beneficiário 78 anos → status SUSPENDED ao salvar
- 6º dependente → HTTP 409; dependente em CANCELLED → HTTP 409
- Beneficiário com 15 pagamentos → `GET .../payments` retorna exatamente 12 ordenados desc
- Beneficiário sem pagamentos → lista vazia `[]` (não 404)

### eligibility
- COD-REGIAO=99, renda R$ 10000 → APROVADO com motivo REGIAO_ESPECIAL_99
- Programa ASSISTENCIAL, renda R$ 700, 0 dependentes → REPROVADO

### audit
- DELETE em audit_event → HTTP 405
- Consulta sem filtros retorna eventos EXCLUSAO junto com os demais
- Toda mudança de status grava evento com 6 campos

---

## Out of Scope (desta feature/sprint)

- BR-012: agrupamento de regiões em 5 faixas no relatório gerencial → fase posterior
- Integração Banco Real (MYS-008): bloco comentado descartado (ADR-002)
- Paginação de impressora 66 linhas: sem valor em API REST moderna
- Relatórios analíticos complexos: `BATCHREL` / `RELPGT` na íntegra → fase posterior

---

## Decisões Arquiteturais Vinculadas

| ADR | Decisão | Impacto nos tasks |
|-----|---------|-------------------|
| ADR-001 | Modular Monolith | Package-by-feature: `com.sifap.{payment,beneficiary,...}` |
| ADR-002 | Strangler Fig | Ordem de implementação: payment → beneficiary → eligibility → program → audit |
| ADR-003 | OAuth2/JWT Gov.br | Spring Security Resource Server em cada controller |
| ADR-004 | Status canônico | Enum `PaymentStatus` em shared-kernel |
| ADR-005 | Fator K externalizável | `@Value("${sifap.payment.fator-k:0.347215}")` em ProgramService |
