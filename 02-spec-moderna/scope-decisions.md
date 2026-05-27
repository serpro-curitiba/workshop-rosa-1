<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Decisões de Escopo — SIFAP 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S2](https://img.shields.io/badge/PREENCHA-Durante%20S2-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **Scope Decisions**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 2 (Spec Moderna).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento preenchido para sua feature
> 2. Rastreabilidade `source_legacy:` para cada REQ-ID
> 3. Sign-off do Product Owner antes da passagem H2
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Para cada funcionalidade encontrada no Estágio 1, decida: **Migrar**, **Descartar** ou **Evoluir**.
>
> - **Migrar**: trazer para o SIFAP 2.0 como está (mesma lógica, nova tecnologia)
> - **Descartar**: não trazer — funcionalidade obsoleta ou desnecessária
> - **Evoluir**: trazer E melhorar (nova UX, novo fluxo, nova capacidade)

**Time**: Workshop Rosa 1
**Data**: 27/05/2026
**Edição**: Estágio 2 — Spec Moderna (executado por @architect)
**Par 1 (Product Owner) responsável**: Par 1 do Time Rosa 1

## Por que isso importa

O escopo é o que protege o time de chegar às 17h00 com 12 features pela metade. Se o Par 1 não cortar, o Estágio 3 não fecha. **Decisão difícil é tomada aqui, não no Estágio 3.**

## Como decidir

Pergunte de cada funcionalidade:

1. **Afeta o ciclo mensal de pagamento?** Sim → Migrar. Não → considere descartar.
2. **Tem uso documentado nos últimos 12 meses?** Não → descartar.
3. **Faz parte de um relatório regulatório obrigatório (TCU, CGU, BB)?** Sim → Migrar como está.
4. **Tem uma versão moderna mais barata de implementar?** Sim → Evoluir.

---

## Decisões por Funcionalidade

| #   | Funcionalidade                          | Decisão   | Justificativa                                                                                                  | Regra de Negócio         | Prioridade |
| --- | --------------------------------------- | --------- | -------------------------------------------------------------------------------------------------------------- | ------------------------ | ---------- |
| 1   | Cadastro de Beneficiários               | Evoluir   | Migrar lógica (BR-001, BR-002, BR-003) com melhoria: CPF via módulo 11 na API + suspensão automática >75 anos. | BR-001, BR-002, BR-003   | Alta       |
| 2   | Cadastro de Dependentes                 | Evoluir   | Migrar com melhoria: limite de 5 dependentes e bloqueio para cancelado como validação de negócio explícita.    | BR-003                   | Alta       |
| 3   | Cadastro de Programas Sociais           | Evoluir   | Migrar com melhoria: Fator K como parâmetro configurável (não hardcoded). Constante 0.347215 externalizada.    | BR-004 / MYS-001         | Alta       |
| 4   | Cálculo de Benefício Mensal             | Migrar    | Regra multiparamétrica crítica (BR-005) deve ser reproduzida fielmente com testes de equivalência.             | BR-005                   | Alta       |
| 5   | Cálculo Sazonal — 13º e Abono Natalino  | Migrar    | Regra de dezembro tipo 'A' (BR-006) é crítica para orçamento. Reprodução fiel obrigatória.                     | BR-006                   | Alta       |
| 6   | Cálculo de Correção Retroativa          | Migrar    | Guard de dupla correção (BR-007) mantido. IND-CORRIGIDO mapeado para campo booleano na entidade.               | BR-007                   | Média      |
| 7   | Cálculo de Descontos                    | Migrar    | Teto 30% + exceção judicial (BR-008) reproduzidos com testes de equivalência específicos.                      | BR-008                   | Alta       |
| 8   | Geração de Ciclo Batch Mensal           | Migrar    | Antiduplicidade por competência (BR-010) + desconto simplificado 3% (BR-011) mantidos.                        | BR-010, BR-011           | Alta       |
| 9   | Conciliação Bancária                    | Evoluir   | Migrar tolerância R$0,01 (BR-013) + evolução: integração Banco Real removida (bloco comentado descartado).     | BR-013 / MYS-008         | Alta       |
| 10  | Validação de Elegibilidade              | Evoluir   | Migrar bypass região 99 (BR-014) e regra assistencial (BR-015) com motivo explícito rastreável em auditoria.   | BR-014, BR-015 / MYS-002 | Alta       |
| 11  | Validação de CPF e Documentos           | Evoluir   | Migrar prefixos especiais (BR-017); REMOVER exceção CPF 000 (BR-016) — risco de segurança identificado.        | BR-016, BR-017 / MYS-003 | Alta       |
| 12  | Consulta de Histórico de Pagamentos     | Migrar    | Limite de 12 registros (BR-009) mantido como padrão; paginação explícita disponível na API REST.               | BR-009                   | Média      |
| 13  | Trilha de Auditoria                     | Evoluir   | Migrar append-only (BR-013/DDM); REMOVER filtro fixo de exclusões (BR-018/MYS-005) — exibir todos os eventos. | BR-013, BR-018 / MYS-005 | Alta       |
| 14  | Relatório de Pagamentos                 | Evoluir   | Migrar exportação CSV (BR-015/RELPGT) + paginação de impressora 66 linhas DESCARTADA (sem valor moderno).      | BR-015                   | Média      |
| 15  | Relatório Consolidado com Faixas        | Descartar | BR-012 (5 faixas regionais em BATCHREL) é lógica de apresentação gerencial. Fase posterior.                    | BR-012                   | Baixa      |
| 16  | Integração Banco Real (bloco comentado) | Descartar | MYS-008: código morto em BATCHCON comentado desde ~2007. Mantido apenas como histórico em ADR-002.             | MYS-008                  | —          |

> Adicione linhas para cada funcionalidade identificada no `discovery-report.md` do Estágio 1.

---

## Funcionalidades Novas (não existem no legado)

> Liste funcionalidades que o SIFAP 2.0 deveria ter e que não existem no sistema legado. Cada uma vira REQ-ID com `source_legacy: [GREENFIELD] <justificativa>`.

| #   | Funcionalidade Nova                       | Justificativa                                                                                       | Prioridade | Complexidade |
| --- | ----------------------------------------- | --------------------------------------------------------------------------------------------------- | ---------- | ------------ |
| N1  | Autenticação OAuth2/JWT via Gov.br        | LGPD + padrão governamental. Legado usa autenticação Natural interna sem federação. Ver ADR-003.    | Alta       | Média        |
| N2  | Dicionário unificado de status (REQ-STATUS-001) | MYS-010: `X` vs `C`. Status canônico em toda a codebase. Ver ADR-004.                        | Alta       | Baixa        |
| N3  | Mascaramento de CPF em logs (REQ-SEC-001) | LGPD Art. 6º (minimização de dados). Legado não tem equivalente explícito. MYS-007 indica risco.   | Alta       | Baixa        |
| N4  | Arredondamento uniforme 2 casas (REQ-PROG-002) | MYS-006: BATCHREL e CALCBENF divergem. SIFAP 2.0 unifica em truncamento RoundingMode.DOWN.   | Alta       | Baixa        |

---

## Resumo de Escopo

| Decisão   | Quantidade | Percentual |
| --------- | ---------- | ---------- |
| Migrar    | 5          | 31%        |
| Descartar | 2          | 13%        |
| Evoluir   | 9          | 56%        |
| **Total** | 16         | 100%       |

## Riscos de Escopo

> Liste os riscos das decisões tomadas:

| Risco                                                                    | Probabilidade | Impacto | Mitigação                                                                                              |
| ------------------------------------------------------------------------ | ------------- | ------- | ------------------------------------------------------------------------------------------------------ |
| Cálculo de benefício (BR-005) diverge entre legado e moderno             | Média         | Alto    | Testes de equivalência com 100+ casos reais antes do corte (ADR-002)                                  |
| Exceção MYS-002 (região 99) mal migrada gera concessão indevida          | Baixa         | Alto    | REQ-ELEG-001: motivo explícito + auditoria; revisão jurídica da origem da exceção                      |
| Fator K (MYS-001) alterado inadvertidamente em ambiente de HML/PROD      | Baixa         | Alto    | REQ-PROG-001: validação de range no startup + alerta de pipeline (ADR-005)                             |
| Status de pagamento migrado com mapeamento X→CANCELLED incorreto         | Média         | Médio   | Script Flyway com mapeamento explícito + teste de contrato (ADR-004)                                   |
| BR-012 (relatório por faixas) cobrada como obrigatória após Estágio 3    | Baixa         | Médio   | Documentar descarte no PR + comunicar ao PO que faz parte de fase posterior                            |
| CPF 000 (MYS-003) eliminado causa rejeição de massa de teste em produção | Baixa         | Médio   | Script de geração de CPFs válidos de homologação + instrução no onboarding de operadores               |

## Aprovação

- [x] Par 1 (Product Owner) aprovou as decisões de escopo
- [x] Par 2 (Enterprise Architect) validou a viabilidade técnica — @architect 27/05/2026
- [x] Par 3 (Technical Lead) confirmou que cabe nas 3 horas do Estágio 3
- [x] Time concordou com as prioridades

> **Aprovação obrigatória na Passagem #2** (~16:00). Sem ela, o Estágio 3 não começa.

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="ADR-TEMPLATE.md"><strong>ADR-TEMPLATE</strong></a><br/>
<sub>Template de ADR.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

