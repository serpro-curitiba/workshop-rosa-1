# Plan: SIFAP 2.0 — Plano Técnico de Implementação

> Gerado por: @architect via /speckit.plan
> Data: 27/05/2026
> Fonte: spec.md + ADR-001 a ADR-005
> Entrega para: Par 3 (Technical Lead + Developer) e Par 4 (DBA + QA) no Estágio 3

---

## 1. Estrutura do Projeto

```
sifap-backend/
  src/main/java/com/sifap/
    shared/                          ← shared-kernel
      domain/
        PaymentStatus.java           ← enum canônico (ADR-004)
        Money.java                   ← value object com truncamento (REQ-PROG-002)
      audit/
        AuditEventPublisher.java     ← interface para publicação de eventos
    beneficiary/                     ← bounded context
      domain/
        BeneficiaryEntity.java
        DependentEntity.java
        DocumentValidationPolicy.java
      application/
        BeneficiaryService.java
        EligibilityService.java      ← delega para eligibility/
      infrastructure/
        BeneficiaryRepository.java
        BeneficiaryController.java
    payment/
      domain/
        PaymentEntity.java
        DiscountEntity.java
      application/
        PaymentCalculationService.java   ← fórmula BR-005, BR-006
        DiscountService.java             ← teto 30%, judicial (BR-008)
        PaymentBatchService.java         ← antiduplicidade BR-010, desconto BR-011
        ReconciliationService.java       ← divergência R$0,01 BR-013
      infrastructure/
        PaymentRepository.java
        PaymentController.java
        BatchJobConfig.java              ← Spring Batch
    eligibility/
      domain/
        EligibilityDecision.java
        EligibilityRule.java             ← interface (Strategy pattern)
        RegionSpecialRule.java           ← REQ-ELEG-001 (região 99)
        AssistentialIncomeRule.java      ← REQ-ELEG-002
      application/
        EligibilityEvaluationService.java
      infrastructure/
        EligibilityController.java
    program/
      domain/
        ProgramEntity.java
      application/
        ProgramService.java              ← FATOR_K via @Value (ADR-005)
      infrastructure/
        ProgramRepository.java
        ProgramController.java
    audit/
      domain/
        AuditEvent.java                  ← append-only (REQ-AUD-001)
      application/
        AuditService.java
      infrastructure/
        AuditEventRepository.java        ← sem save/update/delete no repository
        AuditController.java

sifap-frontend/
  app/
    (auth)/login/page.tsx
    dashboard/page.tsx
    beneficiaries/
      page.tsx                     ← listagem
      [id]/page.tsx                ← detalhe + histórico (REQ-HIST-001)
      new/page.tsx                 ← cadastro
    payments/
      page.tsx                     ← ciclo mensal
      [id]/page.tsx
    audit/
      page.tsx                     ← trilha (REQ-AUD-002)
    programs/
      [id]/page.tsx                ← fator K (REQ-PROG-001)
    components/
      BeneficiaryForm.tsx
      PaymentStatusBadge.tsx
      AuditEventTable.tsx
```

---

## 2. Modelo de Dados (migrations Flyway)

### V1 — Schema base

```sql
-- V1__init_schema.sql

CREATE TYPE beneficiary_status AS ENUM
  ('ACTIVE', 'SUSPENDED', 'INACTIVE', 'CANCELLED');

CREATE TYPE payment_status AS ENUM
  ('PENDING', 'APPROVED', 'REJECTED', 'RECONCILED', 'DIVERGENT', 'CANCELLED');

CREATE TYPE program_type AS ENUM ('ASSISTENCIAL', 'CONTRIBUTIVO');

CREATE TABLE program (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(200) NOT NULL,
  type program_type NOT NULL,
  base_value NUMERIC(12,2) NOT NULL,
  adjustment_factor NUMERIC(10,6) NOT NULL,
  fator_k NUMERIC(10,6) NOT NULL DEFAULT 0.347215,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
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
  reference_year_month VARCHAR(6) NOT NULL,  -- YYYYMM
  gross_amount NUMERIC(12,2) NOT NULL,
  net_amount NUMERIC(12,2),
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
  decision VARCHAR(20) NOT NULL,  -- APPROVED, REJECTED
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
-- SEM índice de update/delete: role da aplicação não tem UPDATE/DELETE nesta tabela
-- REQ-AUD-001: constraint em nível de role PostgreSQL

CREATE TABLE special_cpf_prefix (
  prefix VARCHAR(3) PRIMARY KEY,
  description TEXT,
  created_by VARCHAR(200) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### V2 — Migração de status legado

```sql
-- V2__migrate_payment_status.sql
-- Mapeamento ADR-004: X → CANCELLED, C → CANCELLED
-- (executado durante fase de coexistência Strangler Fig)
UPDATE payment SET status = 'CANCELLED'
  WHERE status::text IN ('X', 'C');
```

---

## 3. Contratos de API (OpenAPI resumido)

```yaml
# /api/v1/beneficiaries
POST   /api/v1/beneficiaries          → 201 Created | 400 CPF inválido
GET    /api/v1/beneficiaries/{id}     → 200 | 404
PATCH  /api/v1/beneficiaries/{id}     → 200 | 409 (status inválido)
POST   /api/v1/beneficiaries/{id}/dependents → 201 | 409 (limite/cancelado)
DELETE /api/v1/beneficiaries/{id}/dependents/{depId} → 204

# /api/v1/programs
GET    /api/v1/programs               → 200
PUT    /api/v1/programs/{id}          → 200 | 400 (fator_k inválido)

# /api/v1/eligibility
POST   /api/v1/eligibility/evaluate   → 200 { decision, reason }

# /api/v1/payment-cycles
POST   /api/v1/payment-cycles         → 202 Accepted (batch assíncrono)
GET    /api/v1/payment-cycles/{month} → 200 { payments[], summary }

# /api/v1/payments
GET    /api/v1/payments/{id}          → 200
PATCH  /api/v1/payments/{id}/approve  → 200 | 409 (status != PENDING)
PATCH  /api/v1/payments/{id}/reject   → 200 | 409

# /api/v1/reconciliation
POST   /api/v1/reconciliation         → 202 (arquivo de retorno)

# /api/v1/beneficiaries/{id}/payments
GET    /api/v1/beneficiaries/{id}/payments → 200 (últimos 12, paginável)

# /api/v1/audit-events
GET    /api/v1/audit-events           → 200 (todos, incluindo EXCLUSAO)
GET    /api/v1/audit-events?entity_type=payment&entity_id={id} → 200
```

---

## 4. Regras de Implementação Críticas

### 4.1 Truncamento financeiro (REQ-PROG-002)

```java
// shared/domain/Money.java
public record Money(BigDecimal amount) {
    public static Money of(BigDecimal value) {
        return new Money(value.setScale(2, RoundingMode.DOWN));
    }
    public Money add(Money other) { return Money.of(this.amount.add(other.amount)); }
    public Money multiply(BigDecimal factor) { return Money.of(this.amount.multiply(factor)); }
}
```

> **NUNCA** use `RoundingMode.HALF_UP` em cálculos financeiros do SIFAP. Sempre `DOWN`.

### 4.2 Enum canônico de status (ADR-004)

```java
// shared/domain/PaymentStatus.java
public enum PaymentStatus {
    PENDING, APPROVED, REJECTED, RECONCILED, DIVERGENT, CANCELLED;
}
```

### 4.3 Fator K externalizável (ADR-005)

```java
// program/application/ProgramService.java
@Value("${sifap.payment.fator-k:0.347215}")
private BigDecimal fatorK;

public Money calculateAdjustedBaseValue(Money baseValue, BigDecimal adjustmentFactor) {
    BigDecimal multiplier = BigDecimal.ONE.add(adjustmentFactor.multiply(fatorK));
    return baseValue.multiply(multiplier);  // truncamento via Money.multiply()
}
```

### 4.4 Auditoria automática (REQ-AUD-003)

```java
// shared/audit/AuditEventPublisher.java (interface)
void publish(String entityType, UUID entityId, String action,
             Object previousState, Object newState, String actor);

// Implementado via Spring ApplicationEvent ou AOP @AfterReturning
// em toda transição de status de PaymentEntity, BeneficiaryEntity, EligibilityDecision
```

### 4.5 CPF mascarado em logs (REQ-SEC-001)

```java
// Configurar PatternLayout no logback.xml:
// <pattern>%d %level %msg%n</pattern>
// Converter customizado: CpfMaskingConverter
// Padrão: \d{3}\.\d{3}\.(\d{3})-(\d{2}) → XXX.XXX.$1-$2
```

---

## 5. Estratégia de Testes

| Tipo | Ferramenta | Cobertura alvo |
|------|-----------|----------------|
| Unidade (lógica de negócio) | JUnit 5 + AssertJ | ≥ 80% módulos payment, eligibility |
| Integração (repository + DB) | Testcontainers + PostgreSQL | Todos os repositories |
| Equivalência legado vs. moderno | JUnit 5 parametrizado | BR-005, BR-006, BR-008 (50+ casos) |
| API (controller) | MockMvc / WebTestClient | Todos os endpoints críticos |
| Frontend | Vitest + Testing Library | BeneficiaryForm, PaymentStatusBadge |

### Testes de equivalência obrigatórios (antes do corte Strangler Fig)

```java
// payment/application/PaymentCalculationServiceEquivalenceTest.java
@ParameterizedTest(name = "BR-005 equivalência legado caso #{index}")
@CsvFileSource(resources = "/test-data/payment-calculation-legacy-cases.csv")
void shouldMatchLegacyCalculation(String region, int members,
                                   BigDecimal income, int age,
                                   BigDecimal expectedGross) {
    var result = service.calculateGrossAmount(region, members, income, age, program);
    assertThat(result.amount()).isEqualByComparingTo(expectedGross);
}
```

---

## 6. Configuração Spring Boot

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sifap
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

sifap:
  payment:
    fator-k: 0.347215         # ADR-005: sobrescrever via SIFAP_FATOR_K env var
  security:
    oauth2:
      resource-server:
        jwt:
          issuer-uri: https://sso.acesso.gov.br/auth/realms/govbr  # ADR-003
```

---

## 7. Dependências Maven (pom.xml core)

```xml
<!-- Java 21 + Spring Boot 3.3 -->
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.3.0</version>
</parent>

<dependencies>
  <dependency><!-- API REST --><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId></dependency>
  <dependency><!-- JPA --><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><!-- OAuth2 Resource Server (ADR-003) --><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId></dependency>
  <dependency><!-- Bean Validation --><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId></dependency>
  <dependency><!-- Spring Batch (ciclo mensal) --><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId></dependency>
  <dependency><!-- PostgreSQL --><groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
  <dependency><!-- Flyway --><groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId></dependency>
  <dependency><!-- OpenAPI/Swagger --><groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version></dependency>
  <!-- Test -->
  <dependency><groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
  <dependency><groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId><scope>test</scope></dependency>
</dependencies>
```

---

## 8. Notas de Pesquisa (Research Notes)

| Tópico | Conclusão | Fonte |
|--------|-----------|-------|
| Constante 0.347215 | Hipótese: calibração histórica de lote pré-1997. Externalizar como FATOR_K. | ADR-005 / MYS-001 |
| Status X vs C | Adabas usa 'X', Natural reports usam 'C'. Unificar em CANCELLED. | ADR-004 / MYS-010 |
| Região 99 | Hipótese: atendimento diplomático/especial. Manter com motivo rastreável. | MYS-002 |
| CPF prefixo 000 | Massa de teste histórica em produção. Eliminar — usar CPFs válidos de homologação. | MYS-003 |
| Filtro EX auditoria | Redução de volume operacional. Remover filtro fixo; filtrar na UI se necessário. | MYS-005 |
| Arredondamento BATCHREL vs CALCBENF | BATCHREL usa 2 casas, CALCBENF usa 3. Unificar em 2 casas (DOWN). | MYS-006 |

---

## 9. Infraestrutura Docker

### 9.1 Decisões de imagem base

| Stage | Imagem | Motivo |
|-------|--------|--------|
| **build** | `eclipse-temurin:21-jdk-alpine` | JDK completo para `./mvnw package`; Alpine reduz tempo de pull |
| **runtime** | `eclipse-temurin:21-jre-alpine` | Somente JRE (~120 MB menor que JDK); sem ferramentas de compilação em produção |

> **Sem dependência de `JAVA_HOME` do host.** O JDK vem exclusivamente da imagem base.
> Variáveis do host (`JAVA_HOME`, `PATH` local) são ignoradas pelo daemon Docker.

### 9.2 Estratégia multi-stage

```
Stage 1 — build
  └── eclipse-temurin:21-jdk-alpine
      ├── Copia .mvn/ + mvnw + pom.xml  → baixa dependências (cache layer)
      ├── Copia src/                     → compila e empacota
      └── Produz target/*.jar

Stage 2 — runtime
  └── eclipse-temurin:21-jre-alpine
      ├── Cria usuário não-root "sifap"
      ├── Copia apenas o *.jar do Stage 1
      └── ENTRYPOINT java -jar app.jar
```

A separação em dois stages garante que o JDK, o Maven e o cache `~/.m2` **não entram na imagem final**.

### 9.3 Dockerfile — `prototype/backend/Dockerfile`

```dockerfile
# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia o wrapper e o POM antes do código-fonte para maximizar cache de camadas.
# Se apenas src/ mudar, o `dependency:go-offline` não é re-executado.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

# Compila e empacota; testes são responsabilidade do pipeline CI (TASK-037).
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuário não-root — princípio do menor privilégio (OWASP A05)
RUN addgroup -S sifap && adduser -S sifap -G sifap
USER sifap

# Copia apenas o JAR final; nada do JDK ou do Maven entra aqui
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# JVM flags recomendadas para containers: usa cgroup limits e imprime GC info
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
```

### 9.4 Critérios de aceite (TASK-038 expandida)

- `docker compose build backend` conclui sem erros partindo de `prototype/backend/` vazio de artefatos locais
- A imagem final **não contém** `javac`, `mvn` nem o diretório `~/.m2`
- `docker compose up` → backend responde em `http://localhost:8080/actuator/health` com `{"status":"UP"}`
- Trocar o JDK na máquina do desenvolvedor **não afeta** o build (sem `JAVA_HOME` no `docker-compose.yml`)
