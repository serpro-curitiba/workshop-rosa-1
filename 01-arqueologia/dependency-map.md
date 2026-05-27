<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mapa de Dependências — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **dependency-map**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Use diagramas Mermaid para mapear as dependências entre programas Natural e DDMs Adabas.
> O objetivo é visualizar "quem chama quem" e "quem lê/escreve o quê".

## Como descobrir dependências

- Use `grep` ou Copilot Chat para listar todas as ocorrências de `CALLNAT` nos 15 arquivos `.NSN`.
- Prompt útil: _"Liste todas as ocorrências de CALLNAT nestes arquivos e desenhe um diagrama Mermaid."_
- Para leitura/escrita em DDMs: procure por `READ`, `READ LOGICAL`, `STORE`, `UPDATE`, `DELETE`.

## Diagrama de Dependências entre Programas

> Substitua o exemplo abaixo pelo mapa real do seu time. **Meta:** cobrir todos os 15 programas, sem órfãos.

```mermaid
flowchart TD
 subgraph "Cadastro e Consulta"
 CADBENEF["CADBENEF.NSN"]
 CADDEPEND["CADDEPEND.NSN"]
 CADPROG["CADPROG.NSN"]
 CONSBENF["CONSBENF.NSN"]
 end

 subgraph "Validacao e Elegibilidade"
 VALBENEF["VALBENEF.NSN"]
 VALDOCS["VALDOCS.NSN"]
 VALELEG["VALELEG.NSN"]
 end

 subgraph "Calculo e Processamento"
 CALCBENF["CALCBENF.NSN"]
 CALCDSCT["CALCDSCT.NSN"]
 CALCCORR["CALCCORR.NSN"]
 BATCHPGT["BATCHPGT.NSN"]
 BATCHCON["BATCHCON.NSN"]
 BATCHREL["BATCHREL.NSN"]
 end

 subgraph "Relatorios"
 RELPGT["RELPGT.NSN"]
 RELAUDIT["RELAUDIT.NSN"]
 end

 subgraph "DDMs Adabas"
 DDM_BENEF[("BENEFICIARIO.ddm")]
 DDM_PAG[("PAGAMENTO.ddm")]
 DDM_PROG[("PROGRAMA-SOCIAL.ddm")]
 DDM_AUD[("AUDITORIA.ddm")]
 end

 CADBENEF -->|FIND/STORE/UPDATE| DDM_BENEF
 CADDEPEND -->|FIND/UPDATE| DDM_BENEF
 CADPROG -->|FIND/STORE| DDM_PROG
 CONSBENF -->|FIND| DDM_BENEF
 CONSBENF -->|READ| DDM_PAG

 VALBENEF -->|Valida campos de entrada| DDM_BENEF
 VALDOCS -->|Valida documentos de entrada| DDM_BENEF
 VALELEG -->|FIND| DDM_BENEF
 VALELEG -->|FIND| DDM_PROG

 CALCBENF -->|FIND| DDM_BENEF
 CALCBENF -->|FIND| DDM_PROG
 CALCBENF -->|STORE| DDM_PAG
 CALCDSCT -->|FIND| DDM_BENEF
 CALCDSCT -->|FIND/UPDATE| DDM_PAG
 CALCCORR -->|READ/UPDATE| DDM_PAG
 BATCHPGT -->|READ| DDM_BENEF
 BATCHPGT -->|FIND| DDM_PROG
 BATCHPGT -->|READ/FIND/STORE| DDM_PAG
 BATCHCON -->|READ/STORE| DDM_AUD
 BATCHCON -->|FIND/UPDATE| DDM_PAG
 BATCHREL -->|READ| DDM_PAG
 BATCHREL -->|FIND| DDM_BENEF

 RELPGT -->|READ| DDM_PAG
 RELPGT -->|FIND| DDM_BENEF
 RELAUDIT -->|READ| DDM_AUD

 CADBENEF -. fluxo de cadastro .-> VALBENEF
 VALBENEF -. validacao aprovada .-> VALELEG
 VALELEG -. elegivel .-> CALCBENF
 CALCBENF -. gera pagamento .-> BATCHCON
 BATCHCON -. base conciliada .-> RELPGT
 BATCHCON -. trilha de auditoria .-> RELAUDIT
```

> **Instrução:** este é apenas um exemplo inicial com 6 programas.
> Seu time deve mapear **todos os 15 programas** e os **4 DDMs**.

## Diagrama de Fluxo de Dados (DDMs)

```mermaid
flowchart LR
 UI["Operadores SIFAP"] --> CAD["Cadastro/Consulta"]
 CAD --> BENEF[("BENEFICIARIO")]
 CAD --> PROG[("PROGRAMA-SOCIAL")]

 VAL["Validacao/Elegibilidade"] --> BENEF
 VAL --> PROG

 CALC["Calculo Batch/Online"] --> BENEF
 CALC --> PROG
 CALC --> PAG[("PAGAMENTO")]

 RET["Retorno CNAB"] --> BCON["BATCHCON"]
 BCON --> PAG
 BCON --> AUD[("AUDITORIA")]

 REL["Relatorios RELPGT/BATCHREL"] --> PAG
 REL --> BENEF
 RELA["RELAUDIT"] --> AUD
```

> Substitua "DDM 3: ???" e "DDM 4: ???" pelos nomes reais encontrados em [`../01-arqueologia/legado-sifap/adabas-ddms/`](../01-arqueologia/legado-sifap/adabas-ddms/).

## Tabela de Dependências

| Programa     | Chama (CALLNAT) | Lê (READ) DDMs | Escreve (STORE/UPDATE) DDMs | Observações |
| ------------ | --------------- | -------------- | --------------------------- | ----------- |
| BATCHCON.NSN | Nenhum | `AUDITORIA`, `PAGAMENTO` | `AUDITORIA`, `PAGAMENTO` | Concilia retorno bancario e registra auditoria de conciliacao/divergencia. |
| BATCHPGT.NSN | Nenhum | `BENEFICIARIO`, `PAGAMENTO`, `PROGRAMA-SOCIAL` | `PAGAMENTO` | Gera pagamento da competencia para beneficiarios ativos. |
| BATCHREL.NSN | Nenhum | `PAGAMENTO`, `BENEFICIARIO` | Nenhum | Consolida valores por regiao e status. |
| CADBENEF.NSN | Nenhum | `BENEFICIARIO` | `BENEFICIARIO` | Inclusao/alteracao de cadastro com validacao de CPF. |
| CADDEPEND.NSN | Nenhum | `BENEFICIARIO` | `BENEFICIARIO` | Manipula grupo periodico de dependentes. |
| CADPROG.NSN | Nenhum | `PROGRAMA-SOCIAL` | `PROGRAMA-SOCIAL` | Mantem parametros de programa e fator de reajuste. |
| CALCBENF.NSN | Nenhum | `BENEFICIARIO`, `PROGRAMA-SOCIAL` | `PAGAMENTO` | Calcula valor bruto/liquido e grava pagamento. |
| CALCCORR.NSN | Nenhum | `PAGAMENTO` | `PAGAMENTO` | Aplica correcao retroativa por indice acumulado. |
| CALCDSCT.NSN | Nenhum | `BENEFICIARIO`, `PAGAMENTO` | `PAGAMENTO` | Aplica descontos por tipo com teto de 30% (exceto judicial). |
| CONSBENF.NSN | Nenhum | `BENEFICIARIO`, `PAGAMENTO` | Nenhum | Consulta cadastral e historico dos ultimos 12 pagamentos. |
| RELAUDIT.NSN | Nenhum | `AUDITORIA` | Nenhum | Emite trilha de auditoria com filtros por periodo/acao/usuario/tabela. |
| RELPGT.NSN | Nenhum | `PAGAMENTO`, `BENEFICIARIO` | Nenhum | Relatorio analitico de pagamentos por periodo e programa. |
| VALBENEF.NSN | Nenhum | Nenhum (entrada de tela) | Nenhum | Valida CPF, data, nome, UF e status informados. |
| VALDOCS.NSN | Nenhum | Nenhum (entrada de tela) | Nenhum | Valida CPF/RG e excecoes de documentos especiais. |
| VALELEG.NSN | Nenhum | `BENEFICIARIO`, `PROGRAMA-SOCIAL` | Nenhum | Determina elegibilidade por renda, idade, status e tipo de programa. |

## Dependências Circulares

> Liste aqui qualquer dependência circular encontrada (programa A chama B que chama A):

- Nenhuma dependencia circular via `CALLNAT` (na amostra, nao ha chamadas `CALLNAT` entre os 15 programas).
- Dependencia indireta por dados existe no ciclo `BATCHPGT -> PAGAMENTO -> BATCHCON -> PAGAMENTO -> RELPGT/BATCHREL`.

## Programas Órfãos

> Programas que não são chamados por nenhum outro (possíveis pontos de entrada ou código morto):

- Todos os 15 programas aparecem no mapa por dependencia de dados; nao ha programa orfao no inventario de arqueologia.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="business-rules-catalog.md"><strong>business-rules-catalog.md</strong></a><br/>
<sub>Catálogo de regras.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="discovery-report.md"><strong>discovery-report.md</strong></a><br/>
<sub>Síntese final.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

