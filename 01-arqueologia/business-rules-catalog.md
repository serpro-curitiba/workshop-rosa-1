<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Catálogo de Regras de Negócio — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **business-rules-catalog**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui todas as regras de negócio extraídas do código Natural/Adabas.
> Cada regra precisa ter rastreabilidade até o código-fonte.
>
> **REGRA DURA:** linhas com `Programa Fonte` vazio são **inválidas** e não contam para o gate do Estágio 2. Use o formato `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>` sempre que possível. Mínimo aceito: nome do arquivo .NSN.

## Como pensar em "regra de negócio"

O que conta:

- Um `IF` que decide algo no domínio (ex.: _"se a UF é do Nordeste e o programa é Seca, valor base × 1.2"_)
- Uma constante numérica sem explicação (ex.: `0.075` num cálculo de imposto)
- Uma transição de status com regra (ex.: _"só de A para S, nunca de I para A"_)
- Um tratamento especial para um caso (ex.: _"se o CPF começa com 999, é teste"_)

O que NÃO conta: paginação de relatório, formatação de saída, manipulação de cursor Adabas, abertura de arquivo. Ignore esses detalhes de implementação.

## Níveis de Risco

| Nível       | Descrição                                                     |
| ----------- | ------------------------------------------------------------- |
| **CRÍTICO** | Regra financeira ou de segurança — erro causa prejuízo direto |
| **ALTO**    | Regra de negócio central — afeta fluxo principal              |
| **MÉDIO**   | Regra de validação ou formatação — afeta qualidade dos dados  |
| **BAIXO**   | Regra de apresentação ou conveniência — impacto limitado      |

## Regras Encontradas

| ID     | Regra de Negócio | Programa Fonte | Campos DDM | Nível de Risco | Notas |
| ------ | ---------------- | -------------- | ---------- | -------------- | ----- |
| BR-001 | Cadastro de beneficiario exige operacao valida (`I` ou `A`) e CPF obrigatorio valido por modulo 11. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L99-L117` | `BENEFICIARIO.NUM-CPF` | ALTO | Bloqueia inclusao/alteracao com documento invalido. |
| BR-002 | Beneficiario com idade acima de 75 inicia/sincroniza status como suspenso (`S`). | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L167-L169` | `BENEFICIARIO.DT-NASCIMENTO`, `BENEFICIARIO.SIT-BENEFICIARIO` | ALTO | Regra de transicao automatica de status no cadastro. |
| BR-003 | Inclusao de dependentes e bloqueada para beneficiario cancelado/desligado e limitada a no maximo 5 dependentes. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L56-L66` | `BENEFICIARIO.SIT-BENEFICIARIO`, `BENEFICIARIO.GRP-DEPENDENTE` | ALTO | Controle de elegibilidade familiar no cadastro. |
| BR-004 | Programa social novo recebe valor base ajustado por fator K (`1 + fator_reaj * 0.347215`). | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L87-L88` | `PROGRAMA-SOCIAL.VLR-BASE-INDIVIDUAL`, `PROGRAMA-SOCIAL.BG(FATOR-K)` | CRÍTICO | Constante magica altera base financeira de todos os calculos. |
| BR-005 | Beneficio mensal usa formula multiparametrica (regional, familiar, renda e idade) com truncamento para 2 casas. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L228-L239` | `BENEFICIARIO.COD-REGIAO`, `BENEFICIARIO.QTD-MEMBROS-FAMILIA`, `BENEFICIARIO.VLR-RENDA-FAMILIAR`, `PAGAMENTO.VLR-BRUTO` | CRÍTICO | Formula central do dominio financeiro. |
| BR-006 | Em dezembro, gera 13o e, para programa tipo `A`, aplica abono natalino de 15%. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L248-L265` | `PROGRAMA-SOCIAL.TIPO-PROGRAMA`, `PAGAMENTO.VLR-BRUTO` | CRÍTICO | Regra sazonal de alto impacto orcamentario. |
| BR-007 | Correção retroativa so e aplicada se `IND-CORRIGIDO != 'S'` e se diferenca calculada for positiva. | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L140-L166` | `PAGAMENTO.IND-CORRIGIDO`, `PAGAMENTO.VLR-CORRECAO`, `PAGAMENTO.DT-CORRECAO` | ALTO | Evita dupla correcao e atualiza flag de controle. |
| BR-008 | Desconto total tem teto de 30% do bruto, exceto desconto judicial (`TIPO-DSCT = 'J'`). | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L102-L169` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-DESCONTO-TOTAL`, `PAGAMENTO.GRP-DESCONTO` | CRÍTICO | Excecao juridica explicita no legado. |
| BR-009 | Consulta de historico de pagamentos retorna no maximo os ultimos 12 registros por beneficiario. | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L151-L158` | `PAGAMENTO.NUM-PAGAMENTO`, `PAGAMENTO.ANO-MES-REF` | MÉDIO | Limite funcional da tela de consulta. |
| BR-010 | No lote de geracao, beneficiario so recebe pagamento se status for ativo e ainda nao existir pagamento da competencia. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L195-L210` | `BENEFICIARIO.SIT-BENEFICIARIO`, `PAGAMENTO.ANO-MES-REF` | CRÍTICO | Evita duplicidade financeira mensal. |
| BR-011 | No batch mensal, desconto simplificado de 3% e aplicado quando valor bruto supera 500.00. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L308-L312` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-DESCONTO-TOTAL` | ALTO | Politica simplificada paralela ao calculo detalhado. |
| BR-012 | Relatorio consolidado agrupa regiao em 5 faixas por codigo (1-5, 6-10, 11-15, 16-20, demais). | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L117-L133` | `BENEFICIARIO.COD-REGIAO`, `PAGAMENTO.VLR-BRUTO` | MÉDIO | Mapeamento impacta indicadores gerenciais. |
| BR-013 | Conciliacao marca divergencia quando diferenca absoluta entre valor SIFAP e banco excede 0.01. | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L155-L202` | `PAGAMENTO.VLR-LIQUIDO`, `PAGAMENTO.COD-RETORNO-BANCO`, `AUDITORIA.DES-ACAO` | CRÍTICO | Corte de centavos define fluxo de divergencia e auditoria. |
| BR-014 | Elegibilidade por regiao especial (`COD-REG = 99`) aprova imediatamente e encerra validacao. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L107-L111` | `BENEFICIARIO.COD-REGIAO` | ALTO | Bypass explicito de regras gerais de elegibilidade. |
| BR-015 | Programa assistencial (`TIPO='A'`) reprova beneficiario com renda > 600 sem dependentes e exige documentacao completa. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L168-L182` | `BENEFICIARIO.VLR-RENDA-FAMILIAR`, `BENEFICIARIO.QTD-MEMBROS-FAMILIA`, `BENEFICIARIO.DOCUMENTOS-OK` | ALTO | Regra de elegibilidade social com condicao composta. |
| BR-016 | Validacao cadastral aceita CPF com todos digitos iguais apenas quando prefixo inicial e `000` (excecao de teste governo). Remover esta regra pois é um risco de segurança. | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L195-L200` | `BENEFICIARIO.NUM-CPF` | ALTO | Excecao nao padrao ao algoritmo de CPF. |
| BR-017 | Validacao documental aceita CPF especial quando prefixo pertence a tabela de prefixos especiais configurada. | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L174-L180` | `BENEFICIARIO.NUM-CPF` | MÉDIO | Caminho alternativo para documentos especiais. |
| BR-018 | Relatorio de auditoria oculta eventos de exclusao (`ACAO = 'EX'`) por filtro fixo de exibicao. | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L105-L108` | `AUDITORIA.COD-ACAO`, `AUDITORIA.DES-ACAO` | ALTO | Impacta transparencia operacional e revisao de trilha. |

> Adicione mais linhas conforme necessário. Lembre-se: existem **10 regras escondidas** no código!

## Exemplo de linha bem preenchida

| ID     | Regra de Negócio                                                                        | Programa Fonte                                   | Campos DDM                                                               | Nível de Risco | Notas                                      |
| ------ | --------------------------------------------------------------------------------------- | ------------------------------------------------ | ------------------------------------------------------------------------ | -------------- | ------------------------------------------ |
| BR-013 | Desconto total não pode exceder 30% do valor bruto, exceto descontos judiciais (tipo J) | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira. Tipo 'J' = exceção legal |

## Regras por Categoria

### Cálculos Financeiros

- BR-004, BR-005, BR-006, BR-007, BR-008, BR-011, BR-013.

### Validações de Status

- BR-002, BR-010, BR-014, BR-018.

### Regras de Autorização

- BR-003, BR-015, BR-017.

### Regras de Negócio Temporais

- BR-006, BR-007, BR-009, BR-010, BR-012.

## Resumo Estatístico

- Total de regras encontradas: 18
- Regras críticas: 5 (BR-004, BR-005, BR-006, BR-008, BR-013)
- Regras com duplicação: 2 (calculo de beneficio em `CALCBENF.NSN` e `BATCHPGT.NSN`; algoritmo CPF em `CADBENEF.NSN`, `VALBENEF.NSN`, `VALDOCS.NSN`)
- Regras sem documentação (escondidas): 8

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
<a href="dependency-map.md"><strong>dependency-map.md</strong></a><br/>
<sub>Mapa de quem chama quem.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

