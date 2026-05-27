<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Glossário do SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **glossary**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Preencha esta tabela com todos os termos, abreviações e siglas encontrados no código Natural/Adabas.
> **Meta: no mínimo 30 termos.**

## Por que isso importa

Sistemas legados têm vocabulário próprio que ninguém documenta em lugar nenhum — só está no nome das variáveis. Se o time do Estágio 2 não souber o que `DSCT`, `BENF`, `PE` ou `CTC` significam, vai escrever uma spec sobre o que ele _acha_ que isso significa. Glossário é o que evita esse desencontro.

## Como preencher

- **Termo**: a abreviação ou sigla exatamente como aparece no código
- **Expansão**: o significado completo do termo
- **Programa**: em qual arquivo `.NSN` ou `.ddm` o termo foi encontrado
- **Contexto**: breve explicação de como/onde o termo é usado

## Dica de extração

Prompt útil no Copilot Chat (cole o conteúdo de 2–3 arquivos `.NSN` no chat antes):

> _"Liste todas as abreviações e siglas usadas neste código Natural. Para cada uma, sugira a expansão e marque com 'CONFIRMADO' ou 'HIPÓTESE'."_

## Termos encontrados

| #   | Termo | Expansão | Programa | Contexto |
| --- | ----- | -------- | -------- | -------- |
| 1   | CPF | Cadastro de Pessoa Fisica | `CADBENEF.NSN`, `BENEFICIARIO.ddm` | Identificador principal de beneficiario e base de validacao modulo 11. |
| 2   | NIS | Numero de Identificacao Social | `VALELEG.NSN`, `BENEFICIARIO.ddm` | Exigido em regras de elegibilidade especifica (`COD-ELEG` com prefixo `R`). |
| 3   | COD-PROGRAMA | Codigo do programa social | `BATCHPGT.NSN`, `PROGRAMA-SOCIAL.ddm` | Relaciona beneficiario ao programa e dirige calculo de beneficio. |
| 4   | COD-REGIAO | Codigo de regiao | `CALCBENF.NSN`, `BATCHREL.NSN`, `BENEFICIARIO.ddm` | Define fator regional e agrupamento de relatorios (01-05 e 99 especial). |
| 5   | VLR-BRUTO | Valor bruto do pagamento | `BATCHPGT.NSN`, `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Base de calculo de descontos e conciliacao. |
| 6   | VLR-LIQUIDO | Valor liquido do pagamento | `BATCHPGT.NSN`, `BATCHCON.NSN`, `PAGAMENTO.ddm` | Valor final pago apos descontos; usado na conciliacao bancaria. |
| 7   | VLR-DESCONTO | Valor total de desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Soma de descontos aplicados por tipo e teto de regra. |
| 8   | COMPETENCIA | Ano/mes de referencia | `BATCHPGT.NSN`, `RELPGT.NSN`, `PAGAMENTO.ddm` | Periodo de processamento e relatorios de pagamento. |
| 9   | STATUS-PGTO | Status do pagamento | `BATCHCON.NSN`, `RELPGT.NSN` | Ciclo de vida do pagamento (`G`, `P`, `D`, `E`, `C`). |
| 10  | STATUS-BENEF | Status do beneficiario | `VALELEG.NSN`, `CADBENEF.NSN` | Estado cadastral (`A`, `S`, `C`, `I`, `D`) com impacto direto em elegibilidade. |
| 11  | TIPO-PROG | Tipo do programa | `VALELEG.NSN`, `CALCBENF.NSN`, `PROGRAMA-SOCIAL.ddm` | Classifica programa em assistencial/previdenciario/trabalho. |
| 12  | TIPO-PGTO | Tipo do pagamento | `BATCHPGT.NSN`, `RELPGT.NSN` | Define pagamento normal/dezembro e exibicao em relatorio. |
| 13  | COD-ELEG | Codigo de elegibilidade | `VALELEG.NSN`, `CADPROG.NSN` | Aciona verificacoes especificas como exigencia de NIS e dependentes. |
| 14  | DT-NASCIMENTO | Data de nascimento | `VALBENEF.NSN`, `BENEFICIARIO.ddm` | Usada para validacao de cadastro e regras de faixa etaria. |
| 15  | NUM-DEPENDENTES | Quantidade de dependentes | `CADDEPEND.NSN`, `CALCBENF.NSN` | Afeta fator familiar e elegibilidade em programas assistenciais. |
| 16  | PARENTESCO | Tipo de parentesco do dependente | `CADDEPEND.NSN`, `BENEFICIARIO.ddm` | Validacao de dominio para dependente (`FI`, `CO`, `IR`, `OU`). |
| 17  | PE | Periodic Group | `BENEFICIARIO.ddm`, `PAGAMENTO.ddm`, `PROGRAMA-SOCIAL.ddm` | Estrutura repetitiva Adabas usada para dependentes, descontos e faixas. |
| 18  | MU | Multiple Value | `AUDITORIA.ddm`, `PROGRAMA-SOCIAL.ddm` | Campo multivalorado Adabas (ex.: campos antes/depois na auditoria). |
| 19  | FATOR-REG | Fator regional | `CALCBENF.NSN`, `BATCHPGT.NSN` | Multiplicador aplicado ao valor base conforme regiao do beneficiario. |
| 20  | FATOR-FAM | Fator familiar | `CALCBENF.NSN`, `BATCHPGT.NSN` | Incremento progressivo pelo numero de dependentes. |
| 21  | FATOR-RND | Fator de renda | `CALCBENF.NSN` | Ajuste por faixa de renda no calculo principal do beneficio. |
| 22  | FATOR-IDADE | Fator por idade | `CALCBENF.NSN`, `BATCHPGT.NSN` | Ajuste de beneficio para faixas `<18`, `>=60`, `>=65`. |
| 23  | FATOR-REAJ | Fator de reajuste | `CADPROG.NSN`, `CALCBENF.NSN` | Parametro de programa para atualizar valor do beneficio. |
| 24  | FATOR-K | Fator K especial | `CADPROG.NSN`, `PROGRAMA-SOCIAL.ddm` | Multiplicador historico pouco documentado aplicado no cadastro de programa. |
| 25  | 13O | Decimo terceiro | `BATCHPGT.NSN`, `CALCBENF.NSN` | Pagamento adicional processado em dezembro (`TIPO-PGTO = 'D'`). |
| 26  | ABONO | Abono natalino | `BATCHPGT.NSN`, `CALCBENF.NSN` | Adicional de 15% para programas tipo `A` no mes 12. |
| 27  | COD-RET | Codigo de retorno bancario | `BATCHCON.NSN`, `PAGAMENTO.ddm` | Resultado do CNAB usado para atualizar status da conciliacao. |
| 28  | CNAB 240 | Layout bancario de retorno | `BATCHCON.NSN` | Estrutura de arquivo de retorno para conciliacao de pagamentos. |
| 29  | IND-CORRIGIDO | Indicador de correcao aplicada | `CALCCORR.NSN` | Evita aplicar correcao retroativa mais de uma vez no mesmo pagamento. |
| 30  | IPCA | Indice de correcao monetaria | `CALCCORR.NSN` | Tabela anual usada para calcular indice acumulado de correcao. |
| 31  | SIT-PROGRAMA | Situacao do programa | `PROGRAMA-SOCIAL.ddm`, `VALELEG.NSN` | Registra se programa esta ativo/inativo e bloqueia elegibilidade. |
| 32  | SIT-CONCILIACAO | Situacao da conciliacao bancaria | `PAGAMENTO.ddm`, `BATCHCON.NSN` | Marca pagamento como conciliado, divergente, pendente ou nao aplicavel. |
| 33  | COD-ACAO | Codigo de acao de auditoria | `AUDITORIA.ddm`, `RELAUDIT.NSN` | Tipifica eventos (`IN`, `AL`, `EX`, `CO`, `DV`, etc.). |
| 34  | ID-CORRELACAO | Identificador de correlacao | `AUDITORIA.ddm` | Liga eventos compostos de auditoria no mesmo fluxo de negocio. |
| 35  | HASH-ARQ-RETORNO | Hash de integridade de arquivo | `PAGAMENTO.ddm` | Campo de rastreabilidade para arquivo de retorno bancario. |

> Adicione mais linhas conforme necessário. Não se limite a 30!

## Exemplo de linha bem preenchida

| #   | Termo  | Expansão | Programa                        | Contexto                                                                                                         |
| --- | ------ | -------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| 1   | `DSCT` | Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Tipo de dedução aplicada sobre valor bruto do pagamento. Tipos: 'J' (judicial), 'I' (imposto), 'T' (trabalhista) |

## Observações

- Anote aqui qualquer padrão de nomenclatura que o time identificou:
- Convenções de prefixo/sufixo encontradas:
	- Prefixo `VLR-` para campos monetarios e `DT-` para datas e frequente em programas e DDMs.
	- Sufixo `-V` em views DDM nos programas Natural (`BENEFICIARIO-V`, `PAGAMENTO-V`, `PROGRAMA-V`, `AUDITORIA-V`).
	- Prefixo `COD-` para dominios enumerados (status, acao, retorno, regiao).
- Termos ambíguos que precisam de validação com especialista:
	- Semantica exata de `FATOR-K` (documentado como especial, mas sem regra formal no legado).
	- Divergencia entre codigos de status em programas (`C` em relatorios vs `X` no DDM de pagamento).
	- Escopo oficial dos prefixos de CPF de teste/governo (`000` em `VALBENEF.NSN` e lista especial em `VALDOCS.NSN`).

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 1</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="business-rules-catalog.md"><strong>business-rules-catalog.md</strong></a><br/>
<sub>Catálogo de regras.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

