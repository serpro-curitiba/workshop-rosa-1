## Plan: Spec Moderna SIFAP 2.0 — Estágio 2 (execução por agente)

O agente vai criar **do zero** os 3 artefatos obrigatórios do Estágio 2, derivando tudo das 18 BRs e 5 mistérios já catalogados no Estágio 1. Nenhuma spec existe ainda em 02-spec-moderna.

---

### Fase 1 — Leitura dos inputs (bloqueia tudo)
Antes de escrever qualquer linha, o agente lê em paralelo:
1. business-rules-catalog.md — fonte de BRs com linhas exatas dos .NSN
2. mysteries-found.md — os 5 mistérios não resolvidos
3. scope-decisions.md — template atual (a substituir)
4. ADR-TEMPLATE.md — estrutura dos ADRs
5. SPECIFICATION-exemplo.md — formato de referência dos REQ-IDs YAML
6. ADR-001-monolito-modular-exemplo.md — referência de ADR preenchido

---

### Fase 2 — Criar `02-spec-moderna/SPECIFICATION.md` *(depende da Fase 1)*

≥18 REQ-IDs em YAML, distribuídos em **4 módulos** + seções de NFR, rastreabilidade BR→REQ e C4 L1+L2 em Mermaid:

| Módulo | REQ-IDs | BRs de origem |
|---|---|---|
| `payment` | REQ-PAY-001 a 008 | BR-005,006,007,008,010,011,013 |
| `beneficiary` | REQ-BEN-001 a 005 | BR-001,002,003,009,016,017 |
| `eligibility` | REQ-ELEG-001 a 002 | BR-014,015 + MYS-002 |
| `program` | REQ-PROG-001 | BR-004 + MYS-001 |
| `audit` | REQ-AUD-001 a 002 | BR-018 + MYS-005 |
| NFRs greenfield | REQ-SEC-001 (CPF/LGPD), REQ-STATUS-001 (MYS-010) | [GREENFIELD] |

**Regra invariante**: todo REQ-ID leva `source_legacy:` com caminho + número de linha do `.NSN` ou `[GREENFIELD] + justificativa`.

---

### Fase 3 — Criar 5 ADRs *(paralelo com Fase 2, após Fase 1)*

| Arquivo | Decisão | Problema que resolve |
|---|---|---|
| `ADR-001.md` | Modular Monolith vs Microsserviços | Escolha arquitetural base |
| `ADR-002.md` | Strangler Fig incremental vs big bang | Estratégia de migração |
| `ADR-003.md` | OAuth2/JWT + Gov.br como IdP | Autenticação e autorização |
| `ADR-004.md` | Dicionário unificado de status de pagamento | MYS-010 (divergência DDM vs relatórios) |
| `ADR-005.md` | Constante 0.347215 como configuração externalizável | MYS-001 (fator K sem origem normativa) |

---

### Fase 4 — Preencher scope-decisions.md *(depende da Fase 1)*

Substituir o conteúdo de template genérico pelos dados reais do `discovery-report.md §5.1-5.3`:

- **Migrar**: ciclo de pagamento (`CALCBENF`+`BATCHPGT`), descontos/conciliação (`CALCDSCT`+`BATCHCON`), elegibilidade/validação (`VALELEG`+`VALBENEF`+`VALDOCS`)
- **Descartar**: bloco Banco Real em `BATCHCON` (comentado), paginação de impressora 66 linhas
- **Evoluir**: trilha de auditoria (remover filtro `ACAO='EX'`), validador de docs (feature flag), dicionário de status

---

### Fase 5 — Verificação *(depende das Fases 2, 3, 4)*

1. `grep -c "source_legacy:" 02-spec-moderna/SPECIFICATION.md` = número de REQ-IDs
2. `ls 02-spec-moderna/ADR-*.md | wc -l` ≥ 3
3. Todo `[GREENFIELD]` tem justificativa na mesma linha
4. SPECIFICATION.md tem blocos `mermaid` para C4 L1 e L2
5. `scope-decisions.md` não contém mais os placeholders do template

---

**Arquivos a criar:**
- 02-spec-moderna/SPECIFICATION.md — criar
- 02-spec-moderna/ADR-001.md a ADR-005.md — criar (5 arquivos)
- scope-decisions.md — sobrescrever com dados reais

**Referências a seguir:**
- SPECIFICATION-exemplo.md — estrutura YAML dos REQ-IDs
- ADR-001-monolito-modular-exemplo.md — seções do ADR
- business-rules-catalog.md — linhas exatas por BR
- mysteries-found.md — MYS-001/002/003/005/010

---

**Decisões tomadas:**
- Os 5 mistérios tornam-se REQ-IDs explícitos, não "notas de implementação"
- C4 L3 fora de escopo (apenas L1 e L2)
- Status unificado via `ADR-004`: `PENDING | APPROVED | REJECTED | RECONCILED | CANCELLED`
- 18 REQ-IDs (1 por BR) é o alvo; mínimo obrigatório do DoD é 12

