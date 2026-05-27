<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: Workshop Rosa 1
**Data**: 27/05/2026
**Edição**: Estagio 1 - Arqueologia executada
**Participantes**: Par 1 (PO + RE) com contribuicao dos pares 2, 3, 4 e 5

---

## 1. Sumário Executivo

> Em 3 a 5 frases, resuma o que o time descobriu sobre o SIFAP legado.
> O que é este sistema? Qual sua criticidade? Qual o estado do código?

O SIFAP legado e um monolito Natural/Adabas orientado a cadastro de beneficiarios, calculo de beneficios e conciliacao bancaria com trilha de auditoria. A analise dos 15 programas `.NSN` e 4 DDMs mostrou regras financeiras criticas espalhadas entre `CALCBENF.NSN`, `CALCDSCT.NSN`, `BATCHPGT.NSN` e `BATCHCON.NSN`, com duplicacao parcial de logica. Foram catalogadas 18 regras de negocio com rastreabilidade de arquivo e linha, incluindo excecoes relevantes (regiao 99, CPF especial, filtro de auditoria). Tambem foram registrados 10 misterios com potencial de risco para migracao, dos quais 5 de alta confianca e impacto direto em compliance/financeiro. O estagio esta apto para a Passagem #1, com insumos completos para EARS no Estagio 2.

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

O SIFAP gerencia o ciclo de beneficios sociais: cadastro e manutencao de beneficiarios/programas, validacao de elegibilidade, calculo mensal e sazonal de pagamentos, aplicacao de descontos, conciliacao de retorno bancario e emissao de relatorios operacionais/auditoria.

### 2.2 Arquitetura Legada

Arquitetura baseada em programas Natural que operam diretamente sobre 4 DDMs Adabas (`BENEFICIARIO`, `PAGAMENTO`, `PROGRAMA-SOCIAL`, `AUDITORIA`). Nao foram encontradas chamadas `CALLNAT` explicitas entre os 15 programas; o acoplamento ocorre majoritariamente por compartilhamento de dados nas mesmas entidades. Fluxos principais identificados:

- Cadastro e validacao: `CADBENEF`, `CADDEPEND`, `CADPROG`, `VALBENEF`, `VALDOCS`, `VALELEG`.
- Calculo e processamento: `CALCBENF`, `CALCDSCT`, `CALCCORR`, `BATCHPGT`, `BATCHCON`.
- Relatorios: `BATCHREL`, `RELPGT`, `RELAUDIT`, `CONSBENF`.

### 2.3 Usuários e Perfis

Perfis inferidos do codigo/estrutura:

- Operador de cadastro/atendimento: usa telas de cadastro e consulta (`CAD*`, `CONSBENF`).
- Processamento batch: usuario tecnico `BATCH` em gravacoes de auditoria de conciliacao.
- Auditoria/controle: consumo de trilha em `RELAUDIT`, com filtros por usuario/acao/tabela.
- Gestao financeira: uso de relatorios analiticos e consolidados (`RELPGT`, `BATCHREL`).

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

> Liste as 5 regras de negócio mais importantes encontradas.

1. Formula central do beneficio multiparametrica com truncamento (BR-005).
2. Regra sazonal de 13o + abono de 15% para tipo `A` em dezembro (BR-006).
3. Teto de desconto em 30% com excecao judicial (BR-008).
4. Antiduplicidade de pagamento por competencia em lote (BR-010).
5. Conciliacao com limite de divergencia de 0.01 e trilha de auditoria (BR-013).

### 3.2 Dependências Complexas

> Quais programas estão mais acoplados? Onde há risco de efeito cascata?

Os maiores pontos de acoplamento sao `PAGAMENTO` e `BENEFICIARIO`, compartilhados por grande parte dos programas. O ciclo `BATCHPGT -> PAGAMENTO -> BATCHCON -> PAGAMENTO -> RELPGT/BATCHREL` cria risco de efeito cascata: alteracao de status/campos em lote impacta conciliacao e dois tipos de relatorio. Outro ponto critico e a duplicacao de logica financeira entre `CALCBENF` e `BATCHPGT`, que pode divergir em evolucoes futuras.

### 3.3 Dívida Técnica Identificada

> Que problemas no código legado vão complicar a migração?

- [x] Constantes magicas sem regra documental (`0.347215`, cortes por centavos, regras de excecao CPF).
- [x] Duplicacao de validacoes e calculos entre programas (CPF e formula de beneficio em mais de um modulo).
- [x] Divergencia semantica de status entre DDM e relatorios (`X` no DDM vs `C` no relatorio de pagamentos).

### 3.4 Gaps de Documentação

> O que a documentação existente NÃO cobre?

Nao ha documento de negocio explicando o fator K, a origem normativa da regiao 99, nem a governanca dos prefixos especiais de CPF. Tambem nao ha matriz oficial de mapeamento de status entre `PAGAMENTO.ddm` e relatorios Natural, o que aumenta risco de interpretacao incorreta na API moderna.

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos

> Resuma os mistérios do arquivo `mysteries-found.md` que permanecem sem explicação.

| ID  | Descrição | Risco para Migração |
| --- | --------- | ------------------- |
| MYS-001 | Constante de ajuste `0.347215` no cadastro de programa | Valores migrados podem divergir historicamente |
| MYS-002 | Bypass de elegibilidade para `COD-REG=99` | Concessao incorreta ou perda de excecao legal |
| MYS-003 | Excecao de CPF `000` como valido | Falha de controle cadastral/compliance |
| MYS-005 | Filtro fixo remove exclusoes da trilha exibida | Auditoria incompleta no monitoramento operacional |
| MYS-010 | Inconsistencia de codigos de status pagamento | API e relatorios modernos podem divergir |

### 4.2 Riscos para o Estágio 2

> O que o time de especificação precisa saber antes de começar?

1. Especificacoes EARS sem preservar excecoes historicas (regiao 99, CPF especial, filtro de auditoria).
2. Consolidacao incorreta de status de pagamento por conflito entre DDM e programas de relatorio.
3. Reescrita de formulas financeiras sem reproduzir truncamentos e limiares do legado.

---

## 5. Recomendações

### 5.1 O que migrar primeiro

> Com base na priorização do Par 1 (Product Owner), quais funcionalidades devem ser migradas primeiro?

| Prioridade | Funcionalidade | Justificativa |
| ---------- | -------------- | ------------- |
| 1          | Geracao e calculo de pagamentos (`CALCBENF` + `BATCHPGT`) | Maior impacto de negocio e dependencia de todo fluxo financeiro |
| 2          | Regras de desconto e conciliacao (`CALCDSCT` + `BATCHCON`) | Garante valor liquido correto e consistencia bancaria |
| 3          | Elegibilidade e validacao (`VALELEG` + `VALBENEF` + `VALDOCS`) | Define quem recebe e evita concessao indevida |

### 5.2 O que descartar

> Funcionalidades que provavelmente não precisam ser migradas:

- Bloco comentado de integracao Banco Real em `BATCHCON`: manter apenas como registro historico, sem migrar para fluxo ativo.
- Renderizacao de relatorio com paginacao de impressora 66 linhas: nao e requisito funcional de dominio para API moderna.

### 5.3 O que evoluir

> Funcionalidades que devem ser migradas E melhoradas:

- Trilha de auditoria: manter integralidade de eventos e remover filtro fixo de exclusoes na camada de apresentacao.
- Validador de documentos: preservar excecoes legadas sob feature flag e trilha de aprovacao explicita.
- Catalogo de status de pagamento: unificar dicionario de estados entre banco de dados, APIs e relatorios.

---

## 6. Métricas do Estágio

| Métrica                       | Valor        |
| ----------------------------- | ------------ |
| Programas analisados          | 15 / 15      |
| DDMs mapeados                 | 4 / 4        |
| Regras de negócio encontradas | 18           |
| Regras escondidas encontradas | 8 / 10       |
| Easter eggs encontrados       | 3 / 3        |
| Termos no glossário           | 35           |
| Mistérios catalogados         | 10           |
| Tempo total gasto             | 3 horas      |

---

## 7. Notas para o Próximo Estágio

> Deixe aqui mensagens para o time no Estágio 2 (Especificação Moderna):

Priorizar a escrita de EARS a partir de BR-005, BR-006, BR-008, BR-010 e BR-013, mantendo `source_legacy` com faixa de linha. Tratar as excecoes (MYS-001, MYS-002, MYS-003, MYS-005, MYS-010) como requisitos explicitos e nao como detalhes de implementacao. Definir no inicio do Estagio 2 um glossario canonicamente aceito para status de pagamento e semantica de fator K.

---

## Definição de Pronto deste relatório

- [x] Todas as seções acima preenchidas (sem placeholders).
- [x] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando uma `BR-XXX` do catálogo.
- [x] Decisões de migrar/descartar/evoluir em §5 cobrem as funcionalidades principais de cadastro, elegibilidade, calculo, conciliacao e relatorios.
- [x] Métricas de §6 conferem com os outros artefatos (glossary.md, business-rules-catalog.md, mysteries-found.md).

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-found.md"><strong>mysteries-found.md</strong></a><br/>
<sub>Lista de mistérios.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec</strong></a><br/>
<sub>Próximo estágio: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

