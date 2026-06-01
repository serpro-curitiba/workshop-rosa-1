# Tasks: SIFAP 2.0 — Estágio 3 (Implementação)

> Gerado por: @architect via /speckit.tasks
> Data: 27/05/2026
> Para: @builder · Par 3 (Technical Lead + Developer) · Par 4 (DBA + QA)
> Pré-requisito: `prototype/backend/` e `prototype/frontend/` começam vazios — o builder gera tudo a partir do zero. FASE 0 cria o `pom.xml` e a estrutura base antes de qualquer outra fase.

---

## Como usar este arquivo

- Marque `[x]` quando a task for concluída.
- Cada task indica: **arquivo a criar/modificar**, **REQ-IDs** implementados e **critério de aceite**.
- Siga a **ordem das fases**: foundation → payment → beneficiary → eligibility → program → audit → batch → frontend → e2e.
- Ao abrir PR, referencie as task IDs no título: `[TASK-003] PaymentEntity + testes`.

---

## FASE 0 — Foundation (shared-kernel + infra)

### TASK-001 · Estrutura de projeto Maven + pom.xml
- [ ] Criar `pom.xml` com dependências declaradas em `plan.md §7`
- [ ] Configurar `application.yml` + `application-test.yml`
- [ ] Configurar `logback-spring.xml` com `CpfMaskingConverter` (REQ-SEC-001)
- **Aceite:** `./mvnw compile` passa sem erros

### TASK-002 · Value object Money + truncamento
- [ ] Criar `com/sifap/shared/domain/Money.java` (record)
- [ ] `RoundingMode.DOWN` em `of()`, `add()`, `multiply()`, `subtract()`
- [ ] Criar `MoneyTest.java` com ≥ 8 casos de truncamento
- **REQ:** REQ-PROG-002
- **Aceite:** `1.555 * 2 = 3.11` (não 3.12); `99.999 = 99.99`

### TASK-003 · Enum PaymentStatus canônico
- [ ] Criar `com/sifap/shared/domain/PaymentStatus.java`
- [ ] Valores: `PENDING, APPROVED, REJECTED, RECONCILED, DIVERGENT, CANCELLED`
- [ ] Nenhuma outra enum de status em todo o projeto
- **REQ:** REQ-STATUS-001 (ADR-004)
- **Aceite:** `PaymentStatus.valueOf("X")` lança `IllegalArgumentException` (sem alias)

### TASK-003b · Enum ProgramType no shared kernel
- [ ] Criar `com/sifap/shared/domain/ProgramType.java`
- [ ] Valores: `ASSISTENCIAL` (código legado 'A', dispara 13º e regra de renda) e `CONTRIBUTIVO`
- [ ] Este tipo é compartilhado entre `payment` e `eligibility` — nunca importar `program.*` nos contextos que o usam
- **REQ:** REQ-PAY-002, REQ-ELEG-002
- **Aceite:** `PaymentCalculationService` e `AssistentialIncomeRule` importam apenas `com.sifap.shared.domain.ProgramType`

### TASK-003c · Enum BeneficiaryStatus no shared kernel
- [ ] Criar `com/sifap/shared/domain/BeneficiaryStatus.java`
- [ ] Valores: `ACTIVE, SUSPENDED, INACTIVE, CANCELLED`
- [ ] Deve coincidir com o ENUM PostgreSQL `beneficiary_status` definido em `V1__init_schema.sql` (TASK-004)
- **REQ:** REQ-BEN-002 (auto-suspensão), REQ-BEN-003 (bloqueio de dependente em CANCELLED)
- **Aceite:** `@Enumerated(EnumType.STRING)` em `BeneficiaryEntity.status` mapeia sem erro; status `CANCELLED` bloqueia criação de dependente (testado em TASK-017)

### TASK-003d · GlobalExceptionHandler (@RestControllerAdvice)
- [ ] Criar `com/sifap/shared/web/GlobalExceptionHandler.java`
- [ ] `EntityNotFoundException` → HTTP 404 `{ "error": "...", "entityId": "..." }`
- [ ] `DuplicateEntityException`, `DiscountCapExceededException`, `DependentLimitExceededException` → HTTP 409
- [ ] `MethodArgumentNotValidException`, `ConstraintViolationException` → HTTP 400 com lista de field errors
- [ ] Nunca expor stack trace — apenas mensagem sanitizada (REQ-SEC-001)
- **REQ:** REQ-SEC-001, ADR-003
- **Aceite:** `EntityNotFoundException` lançada em qualquer controller → 404 JSON sem stack trace; `DiscountCapExceededException` → 409

### TASK-004 · Flyway V1 — schema base
- [ ] Criar `resources/db/migration/V1__init_schema.sql` (conteúdo em `plan.md §2`)
- [ ] Verificar `UNIQUE (beneficiary_id, reference_year_month)` na tabela `payment`
- [ ] Verificar tipos ENUM PostgreSQL para `beneficiary_status` e `payment_status`
- [ ] Criar role `sifap_app` sem `UPDATE/DELETE` em `audit_event`
- **REQ:** REQ-AUD-001, REQ-PAY-006
- **Aceite:** `docker compose up db` + Flyway applies sem erro; `\dt` mostra 7 tabelas

### TASK-005 · SecurityConfig OAuth2 Resource Server
- [ ] Criar `com/sifap/shared/security/SecurityConfig.java`
- [ ] Configurar `http.oauth2ResourceServer(oauth2 -> oauth2.jwt(...))`
- [ ] Roles: `OPERATOR`, `AUDITOR`, `MANAGER`, `ADMIN`
- [ ] `ADMIN` pode tudo; `OPERATOR` não acessa `/api/v1/audit-events`
- **REQ:** ADR-003
- **Aceite:** requisição sem token → 401; token com role OPERATOR em `/audit-events` → 403

### TASK-006 · AuditEventPublisher (interface + implementação)
- [ ] Criar `com/sifap/shared/audit/AuditEventPublisher.java` (interface)
- [ ] Criar `com/sifap/shared/audit/SpringAuditEventPublisher.java` (publica ApplicationEvent)
- [ ] Criar `com/sifap/audit/infrastructure/AuditEventListener.java` (persiste via AuditService)
- **REQ:** REQ-AUD-003
- **Aceite:** nenhum módulo importa `audit/` diretamente — só via interface shared

---

## FASE 1 — Módulo `payment` (P0 — implementar primeiro)

### TASK-007 · PaymentEntity + PaymentRepository
- [ ] Criar `com/sifap/payment/domain/PaymentEntity.java` (@Entity, campos conforme V1__init)
- [ ] Criar `com/sifap/payment/infrastructure/PaymentRepository.java` (JpaRepository)
- [ ] Adicionar `findByBeneficiaryIdAndReferenceYearMonth()` para antiduplicidade
- **REQ:** REQ-PAY-006
- **Aceite:** salvar dois pagamentos com mesmo beneficiário + mês → DataIntegrityViolationException

### TASK-008 · DiscountEntity + DiscountRepository
- [ ] Criar `com/sifap/payment/domain/DiscountEntity.java`
- [ ] Campo `type`: `PREVIDENCIARIO`, `SINDICATO`, `JUDICIAL`, `OUTROS`
- **REQ:** REQ-PAY-004, REQ-PAY-005

### TASK-009 · PaymentCalculationService — fórmula BR-005
- [ ] Criar `com/sifap/payment/application/PaymentCalculationService.java`
- [ ] Implementar fórmula multiparamétrica (ver `02-spec-moderna/SPECIFICATION.md §7`)
- [ ] Usar `Money.of()` em cada etapa — nunca BigDecimal direto
- [ ] Para dezembro e `ProgramType.ASSISTENCIAL` (código 'A' no legado): adicionar 13º + abono 15% — usar `com.sifap.shared.domain.ProgramType` (TASK-003b)
- **REQ:** REQ-PAY-001, REQ-PAY-002
- **Aceite:** 10 casos CSV em `test-data/payment-calculation-legacy-cases.csv` passam

### TASK-010 · DiscountService — teto 30% + judicial sem teto
- [ ] Criar `com/sifap/payment/application/DiscountService.java`
- [ ] Acumular descontos não-judiciais; rejeitar se > 30% do bruto
- [ ] Descontos JUDICIAL: somar sem limite
- [ ] Lançar `DiscountCapExceededException` ao violar teto
- **REQ:** REQ-PAY-004, REQ-PAY-005
- **Aceite:** bruto R$1000, desconto TAX R$350 → excede 30% → exceção; JUDICIAL R$800 → aceito

### TASK-011 · ReconciliationService — tolerância R$0,01
- [ ] Criar `com/sifap/payment/application/ReconciliationService.java`
- [ ] Processar lista de `ReturnFileEntry` (DTO com `paymentId + bankAmount`)
- [ ] Diferença ≤ R$0,01 → status `RECONCILED`; > R$0,01 → `DIVERGENT`
- [ ] Publicar `AuditEvent` para cada mudança de status
- **REQ:** REQ-PAY-008
- **Aceite:** diferença R$0,01 → RECONCILED; R$0,02 → DIVERGENT; evento auditoria gerado

### TASK-012 · CorrectionService — retroativo (REQ-PAY-003)
- [ ] Criar `com/sifap/payment/application/CorrectionService.java`
- [ ] Só corrige se `status == PaymentStatus.APPROVED && corrected == false && delta > 0`
- [ ] Marca `corrected = true` após aplicar
- **REQ:** REQ-PAY-003
- **Aceite:** chamar 2x mesma correção → segunda chamada não altera valor (idempotente)

### TASK-013 · PaymentController — endpoints CRUD + ciclo
- [ ] Criar `com/sifap/payment/infrastructure/PaymentController.java`
- [ ] `POST /api/v1/payment-cycles` → dispara `PaymentBatchService`
- [ ] `GET /api/v1/payment-cycles/{month}` → lista pagamentos do mês
- [ ] `PATCH /api/v1/payments/{id}/approve` e `/reject`
- [ ] Anotações OpenAPI/Swagger em todos os métodos
- **REQ:** REQ-PAY-001 a REQ-PAY-008
- **Aceite:** teste MockMvc para cada endpoint; 401 sem token; 409 em estado inválido

### TASK-014 · Testes de equivalência legado × moderno (obrigatório antes do corte)
- [ ] Criar `payment/application/PaymentCalculationServiceEquivalenceTest.java`
- [ ] Criar `test-data/payment-calculation-legacy-cases.csv` com ≥ 20 casos reais do legado
- [ ] Incluir casos: dezembro, desconto judicial, correção retroativa, batch 3%
- **REQ:** REQ-PAY-001 a REQ-PAY-007 (todos)
- **Aceite:** 0 falhas; outputs da nova lógica idênticos aos esperados pelo legado

---

## FASE 2 — Módulo `beneficiary`

### TASK-015 · BeneficiaryEntity + DependentEntity + Repositories
- [ ] Criar `com/sifap/beneficiary/domain/BeneficiaryEntity.java`; campo `status` do tipo `com.sifap.shared.domain.BeneficiaryStatus` (TASK-003c)
- [ ] Criar `com/sifap/beneficiary/domain/DependentEntity.java`
- [ ] `@OneToMany(mappedBy="beneficiary", cascade=ALL)` + `@PrePersist` para auto-suspensão >75 anos
- **REQ:** REQ-BEN-001, REQ-BEN-002, REQ-BEN-003

### TASK-016 · DocumentValidationPolicy — CPF módulo 11
- [ ] Criar `com/sifap/beneficiary/domain/DocumentValidationPolicy.java`
- [ ] Validar dígito verificador módulo 11 (algoritmo BR)
- [ ] Rejeitar CPF com todos dígitos iguais (000...000 a 999...999) sem exceção (REQ-BEN-004)
- [ ] `isSpecialPrefix(String cpf)`: consulta tabela `special_cpf_prefix` (REQ-BEN-005)
- **REQ:** REQ-BEN-001, REQ-BEN-004, REQ-BEN-005
- **Aceite:** CPF `111.111.111-11` → inválido; CPF de prefixo especial → válido sem módulo 11

### TASK-017 · BeneficiaryService — regras de negócio
- [ ] Validar CPF antes de salvar (delega para `DocumentValidationPolicy`)
- [ ] `@PrePersist`: se `calculateAge(birthDate) > 75` → `status = BeneficiaryStatus.SUSPENDED`
- [ ] Ao adicionar dependente: verificar `status != BeneficiaryStatus.CANCELLED && status != BeneficiaryStatus.INACTIVE` + contar existentes (≤ 5)
- [ ] Publicar `AuditEvent` em toda mudança de status
- **REQ:** REQ-BEN-001 a REQ-BEN-005
- **Aceite:** 6º dependente → HTTP 409 "Limite de dependentes atingido"; beneficiário CANCELLED + dependente → HTTP 409

### TASK-018 · BeneficiaryController — CRUD completo
- [ ] `POST /api/v1/beneficiaries` → 201 | 400 (CPF) | 409 (duplicado)
- [ ] `GET /api/v1/beneficiaries/{id}` → 200 | 404
- [ ] `PATCH /api/v1/beneficiaries/{id}` → 200 | 409
- [ ] `POST /api/v1/beneficiaries/{id}/dependents` → 201 | 409
- [ ] `DELETE /api/v1/beneficiaries/{id}/dependents/{depId}` → 204
- [ ] `GET /api/v1/beneficiaries/{id}/payments` (últimos 12 — REQ-HIST-001)
- **REQ:** REQ-BEN-001 a REQ-BEN-005, REQ-HIST-001

---

## FASE 3 — Módulo `eligibility`

### TASK-019 · EligibilityRule interface + implementações (Strategy)
- [ ] Criar `com/sifap/eligibility/domain/EligibilityOutcome.java` (enum local: `APPROVED`, `REJECTED`) — não importar `PaymentStatus`; este tipo pertence apenas ao contexto `eligibility`
- [ ] Criar `com/sifap/eligibility/domain/EligibilityRule.java` (interface funcional que retorna `EligibilityOutcome`)
- [ ] Criar `RegionSpecialRule.java`: se `codRegion == 99` → `EligibilityOutcome.APPROVED` + motivo `REGIAO_ESPECIAL_99`
- [ ] Criar `AssistentialIncomeRule.java`: se `ProgramType.ASSISTENCIAL` && renda > 600 && dependentes == 0 → `EligibilityOutcome.REJECTED`; usar `com.sifap.shared.domain.ProgramType` (TASK-003b)
- [ ] Criar `EligibilityRuleChain.java`: executar regras em ordem; primeira que decidir ganha
- **REQ:** REQ-ELEG-001, REQ-ELEG-002
- **Aceite:** região 99 + renda R$10000 → APPROVED; ASSISTENCIAL + R$700 + 0 deps → REJECTED

### TASK-020 · EligibilityEvaluationService + Controller
- [ ] Criar `com/sifap/eligibility/application/EligibilityEvaluationService.java`
- [ ] Criar `com/sifap/eligibility/infrastructure/EligibilityController.java`
- [ ] `POST /api/v1/eligibility/evaluate` com body `{ beneficiaryId, programId }`
- [ ] Persistir `EligibilityDecision` para rastreabilidade
- **REQ:** REQ-ELEG-001, REQ-ELEG-002

---

## FASE 4 — Módulo `program`

### TASK-021 · ProgramEntity + ProgramService (FATOR_K)
- [ ] Criar `com/sifap/program/domain/ProgramEntity.java`
- [ ] Criar `com/sifap/program/application/ProgramService.java`
- [ ] `@Value("${sifap.payment.fator-k:0.347215}") BigDecimal fatorK`
- [ ] Validação no `@PostConstruct`: `fatorK > 0 && fatorK <= 1` → falha startup se inválido
- [ ] `calculateAdjustedBaseValue(Money baseValue, BigDecimal factor)` → usa `Money.multiply()`
- **REQ:** REQ-PROG-001, REQ-PROG-002 (ADR-005)
- **Aceite:** `SIFAP_FATOR_K=2.0` → aplicação não sobe (ValidationException); cálculo com 0.347215 reproduz valor legado

### TASK-022 · ProgramController
- [ ] `GET /api/v1/programs` → lista programas
- [ ] `PUT /api/v1/programs/{id}` → atualiza; valida `fator_k` range; 400 se inválido

---

## FASE 5 — Módulo `audit`

### TASK-023 · AuditEvent + AuditService + AuditEventRepository
- [ ] Criar `com/sifap/audit/domain/AuditEvent.java` (campos: entityType, entityId, action, previousState, newState, actor, occurredAt, reason)
- [ ] Criar `com/sifap/audit/infrastructure/AuditEventRepository.java` — apenas `save()` e `findAll()`; sem `delete()` ou `update()`
- [ ] Criar `com/sifap/audit/application/AuditService.java`
- [ ] Escutar `AuditApplicationEvent` publicado pelo `SpringAuditEventPublisher`
- **REQ:** REQ-AUD-001, REQ-AUD-003
- **Aceite:** repositório não expõe `deleteById()`; chamada direta ao service → exceção

### TASK-024 · AuditController — consulta sem filtros fixos
- [ ] `GET /api/v1/audit-events` → retorna TODOS os eventos incluindo `action=EXCLUSAO`
- [ ] Aceitar query params opcionais: `entity_type`, `entity_id`, `from`, `to`
- [ ] Role mínima: `AUDITOR`
- **REQ:** REQ-AUD-002
- **Aceite:** inserir evento EXCLUSAO; GET sem filtros → está na resposta; GET com `entity_type=payment` → filtrado

---

## FASE 6 — Jobs Batch

### TASK-025 · PaymentCycleJob (Spring Batch)
- [ ] Criar `com/sifap/payment/infrastructure/BatchJobConfig.java`
- [ ] Job: `generatePaymentCycleJob` com steps: validação, cálculo, desconto, persistência
- [ ] ItemProcessor: chamar `PaymentCalculationService` + `DiscountService`
- [ ] Antiduplicidade: `ItemWriter` verifica `findByBeneficiaryIdAndReferenceYearMonth()` antes de inserir
- [ ] Desconto batch 3%: se bruto > R$ 500 (REQ-PAY-007)
- **REQ:** REQ-PAY-006, REQ-PAY-007
- **Aceite:** rodar job 2x no mesmo mês → segundo run não cria novos pagamentos

### TASK-026 · ReconciliationJob (Spring Batch)
- [ ] Job: `reconciliationJob` que lê arquivo CSV de retorno bancário
- [ ] Chamar `ReconciliationService` por linha
- [ ] Gerar relatório sumário: total RECONCILED, DIVERGENT, erros
- **REQ:** REQ-PAY-008

---

## FASE 7 — Frontend Next.js

### TASK-027 · Setup Next.js 15 + Tailwind + shadcn/ui
- [ ] `cd prototype/frontend && npx create-next-app@latest . --typescript --tailwind --app --eslint`
- [ ] Adicionar `output: 'standalone'` ao `next.config.js` (necessário para o Dockerfile do TASK-038)
- [ ] Instalar shadcn/ui: `npx shadcn-ui@latest init`
- [ ] Configurar `tsconfig.json` com `strict: true`
- [ ] Criar `lib/api.ts` com fetch helper autenticado (Bearer token)

### TASK-028 · Layout + autenticação
- [ ] Criar `app/layout.tsx` com Sidebar usando shadcn/ui `Sheet`
- [ ] Criar `app/(auth)/login/page.tsx` → redirect para Gov.br OIDC
- [ ] Criar middleware `middleware.ts` para proteger rotas `/dashboard/**`

### TASK-029 · Página de beneficiários
- [ ] Criar `app/beneficiaries/page.tsx` → tabela com paginação (server component)
- [ ] Criar `app/beneficiaries/new/page.tsx` → `BeneficiaryForm` (client component)
- [ ] Criar `components/BeneficiaryForm.tsx` com validação CPF client-side
- [ ] Criar `app/beneficiaries/[id]/page.tsx` → detalhe + últimos 12 pagamentos
- **REQ:** REQ-BEN-001, REQ-HIST-001

### TASK-030 · Página de ciclo de pagamento
- [ ] Criar `app/payments/page.tsx` → gerar ciclo mensal (POST action)
- [ ] Criar `app/payments/[id]/page.tsx` → detalhe + aprovar/rejeitar
- [ ] Criar `components/PaymentStatusBadge.tsx` → mapeamento de cores por status (ADR-004)
- **REQ:** REQ-PAY-001 a REQ-PAY-008

### TASK-031 · Página de auditoria
- [ ] Criar `app/audit/page.tsx` → tabela sem filtros fixos (server component)
- [ ] Filtros opcionais: tipo entidade, data início/fim
- [ ] Mostrar campo `action=EXCLUSAO` com destaque visual
- **REQ:** REQ-AUD-002

### TASK-032 · Configuração de Programa (Fator K)
- [ ] Criar `app/programs/[id]/page.tsx` → exibir e editar `fator_k`
- [ ] Input numérico com validação range (0, 1]
- [ ] Mostrar aviso "Mudança afeta todos os cálculos futuros"
- **REQ:** REQ-PROG-001

---

## FASE 8 — Testes de integração + e2e

### TASK-033 · Testes de integração com Testcontainers
- [ ] Criar `PaymentControllerIntegrationTest.java` (Spring Boot Test + Testcontainers)
- [ ] Cobrir: ciclo gerado, antiduplicidade, desconto acima teto, reconciliação
- [ ] Cobrir: auditoria gravada a cada mudança de status
- **REQ:** REQ-PAY-001 a REQ-PAY-008, REQ-AUD-003

### TASK-034 · Testes de integração beneficiary + eligibility
- [ ] CPF inválido → 400; duplicado → 409; 6º dependente → 409
- [ ] Região 99 → APPROVED; ASSISTENCIAL + renda > 600 + 0 deps → REJECTED
- **REQ:** REQ-BEN-001 a REQ-BEN-005, REQ-ELEG-001, REQ-ELEG-002

### TASK-035 · Testes de segurança
- [ ] Verificar que `audit_event` não pode ser deletado nem pelo endpoint nem pelo repository
- [ ] Verificar que CPF não aparece em logs (testar com `OutputCaptureExtension`)
- [ ] Verificar que `SIFAP_FATOR_K=0` impede startup
- **REQ:** REQ-AUD-001, REQ-SEC-001, REQ-PROG-001 (validação startup)

### TASK-036 · Testes Vitest Frontend
- [ ] Criar `BeneficiaryForm.test.tsx` — validação CPF client-side
- [ ] Criar `PaymentStatusBadge.test.tsx` — todas as variantes de status
- [ ] Criar `AuditEventTable.test.tsx` — exibição de eventos EXCLUSAO

---

## FASE 9 — CI/CD

### TASK-037 · GitHub Actions — pipeline CI
- [ ] Criar `.github/workflows/ci.yml`
- [ ] Jobs: `build` → `test` → `legacy-traceability` (verifica `source_legacy:` em todos REQ-IDs)
- [ ] Job `security-scan`: OWASP Dependency Check
- [ ] Gatilho: todo push em `spec/**` e `develop`

### TASK-038 · Docker Compose — stack completa
- [ ] Criar `prototype/frontend/Dockerfile` conforme spec §9.5 do plan.md
- [ ] Verificar `docker-compose.yml` com serviços: `db` (postgres:16), `backend`, `frontend`
- [ ] Backend health check em `/actuator/health`
- [ ] Flyway roda automaticamente no startup do backend
- [ ] `docker compose up` → frontend responde em `http://localhost:3001`

---

## Resumo de Prioridades

| Fase | Tasks | Responsável sugerido | Prioridade |
|------|-------|---------------------|------------|
| Foundation | TASK-001 a TASK-006 + TASK-003b/c/d | Tech Lead (Par 3A) | P0 — dia 1 manhã |
| payment | TASK-007 a TASK-014 | Developer (Par 3B) + DBA (Par 4A) | P0 — dia 1 tarde |
| beneficiary | TASK-015 a TASK-018 | Developer (Par 3B) | P0 — dia 1 tarde |
| eligibility | TASK-019, TASK-020 | Developer (Par 3B) | P0 — dia 2 manhã |
| program | TASK-021, TASK-022 | Tech Lead (Par 3A) | P0 — dia 2 manhã |
| audit | TASK-023, TASK-024 | DBA (Par 4A) | P0 — dia 2 manhã |
| Batch | TASK-025, TASK-026 | Tech Lead (Par 3A) | P1 — dia 2 tarde |
| Frontend | TASK-027 a TASK-032 | Par 3 (full-stack) | P1 — dia 2 tarde |
| Testes | TASK-033 a TASK-036 | QA (Par 4B) | P0 — ao longo de tudo |
| CI/CD | TASK-037, TASK-038 | DevOps (Par 5A) | P1 — dia 2 |

---

## Mapa REQ-ID → Task

| REQ-ID | Task(s) |
|--------|---------|
| REQ-PAY-001 | TASK-009, TASK-013, TASK-014 |
| REQ-PAY-002 | TASK-003b, TASK-009, TASK-014 |
| REQ-PAY-003 | TASK-012 |
| REQ-PAY-004 | TASK-010, TASK-013 |
| REQ-PAY-005 | TASK-010, TASK-013 |
| REQ-PAY-006 | TASK-007, TASK-025 |
| REQ-PAY-007 | TASK-025 |
| REQ-PAY-008 | TASK-011, TASK-026 |
| REQ-BEN-001 | TASK-016, TASK-017, TASK-018 |
| REQ-BEN-002 | TASK-003c, TASK-015, TASK-017 |
| REQ-BEN-003 | TASK-003c, TASK-017, TASK-018 |
| REQ-BEN-004 | TASK-016 |
| REQ-BEN-005 | TASK-016 |
| REQ-PROG-001 | TASK-021, TASK-032 |
| REQ-PROG-002 | TASK-002, TASK-009 |
| REQ-ELEG-001 | TASK-019, TASK-020 |
| REQ-ELEG-002 | TASK-003b, TASK-019, TASK-020 |
| REQ-AUD-001 | TASK-004, TASK-023, TASK-035 |
| REQ-AUD-002 | TASK-024, TASK-031 |
| REQ-AUD-003 | TASK-006, TASK-023 |
| REQ-SEC-001 | TASK-001, TASK-003d, TASK-035 |
| REQ-STATUS-001 | TASK-003 |
| REQ-HIST-001 | TASK-018, TASK-029 |
