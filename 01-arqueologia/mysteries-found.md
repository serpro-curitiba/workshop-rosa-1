<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **mysteries-found**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui toda lógica, comportamento ou código que o time não conseguiu explicar.
> "Mistérios" são trechos de código sem documentação, com lógica não-óbvia ou que parecem workarounds.
>
> **Cota mínima para passar pelo portão do Estágio 2:** 5 mistérios documentados.

## O que conta como "mistério"?

- Código que faz algo inesperado sem comentário explicando por quê
- Valores hardcoded sem explicação (números mágicos)
- Lógica condicional que parece um workaround ou gambiarra
- Campos no DDM que não são usados por nenhum programa
- Programas que existem mas não são chamados por ninguém
- Comportamento diferente entre o que a documentação diz e o que o código faz
- Easter eggs deixados pelos desenvolvedores originais

## Níveis de Confiança

| Nível     | Significado                                         |
| --------- | --------------------------------------------------- |
| **ALTA**  | Temos certeza de que há algo estranho aqui          |
| **MÉDIA** | Parece suspeito, mas pode ter explicação            |
| **BAIXA** | Pode ser intencional, mas não conseguimos confirmar |

## Mistérios Catalogados

| ID      | Descrição | Onde Encontrado | Impacto Potencial | Confiança |
| ------- | --------- | --------------- | ----------------- | --------- |
| MYS-001 | Constante `0.347215` em `FATOR-K` sem justificativa funcional. | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L87-L88` | Distorce valor base de todos os programas cadastrados. | ALTA |
| MYS-002 | Regiao `99` aprova elegibilidade de forma imediata (bypass das demais validacoes). | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L107-L111` | Risco de concessao indevida se regra for mal migrada ou mal entendida. | ALTA |
| MYS-003 | Excecao de CPF `000...` marcado como valido em validacao completa. | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L195-L200` | Pode introduzir beneficiarios de teste em ambiente real. | ALTA |
| MYS-004 | Prefixos especiais de CPF liberam validacao documental sem esclarecer governanca da lista. | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L174-L180` | Brecha de compliance documental se tabela de prefixo estiver desatualizada. | MÉDIA |
| MYS-005 | Relatorio de auditoria omite acoes de exclusao (`EX`) por regra fixa de exibicao. | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L105-L108` | Perda de visibilidade em trilha de auditoria e risco regulatorio. | ALTA |
| MYS-006 | Comentario explicito de arredondamento diferente entre `BATCHREL` e `CALCBENF`. | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L117-L133` | Divergencia em totais gerenciais versus operacional financeiro. | MÉDIA |
| MYS-007 | Mascara de CPF na consulta tem inconsistencia conhecida e orientacao de nao corrigir sem auditoria. | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L177-L188` | Exposicao de dado sensivel ou mascaramento incorreto em tela. | ALTA |
| MYS-008 | Bloco legado de integracao Banco Real permanece comentado com regra historica. | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L213-L217` | Codigo morto aumenta risco de reativacao acidental e confusao em manutencao. | MÉDIA |
| MYS-009 | DDM de auditoria afirma imutabilidade (`nao permite update/delete`), mas relatorio ainda filtra eventos. | `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm#L9-L12` | Pode haver desalinhamento entre obrigacao legal e visibilidade operacional. | MÉDIA |
| MYS-010 | DDM de pagamento usa status `X=Cancelado`, enquanto relatorios usam `C` como cancelado. | `01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm#L54-L57` | Mapeamento incorreto de status pode quebrar consistencia em APIs futuras. | MÉDIA |

## Detalhamento dos Mistérios

### MYS-001: Constante opaca no fator de programa

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L87-L88`
- **O que esperávamos**: fator de reajuste aplicado diretamente ou por tabela parametrica.
- **O que o código faz**: multiplica reajuste por constante `0.347215` sem fonte de negocio.
- **Hipótese do time**: calibracao historica para compatibilizar lote antigo.
- **Risco se ignorarmos**: pagamentos migrados com base financeira diferente do legado.

### MYS-002: Bypass para regiao especial

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L107-L111`
- **O que esperávamos**: regiao especial com regra propria, mas ainda sujeita a validacoes basicas.
- **O que o código faz**: marca elegivel e encerra rotina imediatamente.
- **Hipótese do time**: atendimento a publico diplomatico/internacional com regra excepcional.
- **Risco se ignorarmos**: concessoes indevidas ou perda de excecao legal obrigatoria.

### MYS-003: CPF de teste governamental

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L195-L200`
- **O que esperávamos**: CPF com todos os digitos iguais sempre invalido.
- **O que o código faz**: abre excecao para prefixo `000` e aceita como valido.
- **Hipótese do time**: massa de homologacao historica usada tambem em producao controlada.
- **Risco se ignorarmos**: regras de antifraude e qualidade cadastral ficam inconsistentes.

### MYS-004: Prefixos especiais sem governanca clara

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L174-L180`
- **O que esperávamos**: politica documentada sobre quais prefixos sao aceitos e por quem.
- **O que o código faz**: consulta lista interna de prefixos especiais e valida sem contexto normativo.
- **Hipótese do time**: mecanismo de excecao para operacao de emergencia/governo.
- **Risco se ignorarmos**: aumento de falso positivo em validacao de documentos.

### MYS-005: Exclusoes ocultas no relatorio de auditoria

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L105-L108`
- **O que esperávamos**: trilha de auditoria exibir todas as acoes, incluindo exclusoes.
- **O que o código faz**: filtra acao `EX` antes de imprimir qualquer registro.
- **Hipótese do time**: reducao de volume de saida em consulta operacional.
- **Risco se ignorarmos**: nao conformidade de auditoria e investigacoes incompletas.

### MYS-006: Divergencia em arredondamento entre batch e online

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L117-L133`
- **O que esperávamos**: arredondamento uniforme em toda a aplicacao para garantir totais consistentes.
- **O que o código faz**: comentario explicito revela que `BATCHREL` arredonda para 2 casas, enquanto `CALCBENF` arredonda para 3.
- **Hipótese do time**: legado de periodos diferentes de implantacao, possivelmente vinculado a regra contabil antiga.
- **Risco se ignorarmos**: totais gerenciais divergentes dos valores financeiros operacionais, gerando contestacao de auditoria.

### MYS-007: Inconsistencia conhecida na mascara de CPF

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L177-L188`
- **O que esperávamos**: mascara de CPF aplicada uniformemente em toda tela de consulta.
- **O que o código faz**: bloco condicional aplica mascara apenas em contexto especifico, com comentario de nao corrigir sem auditoria.
- **Hipótese do time**: workaround historico para evitar quebrar integracao externa que esperava formato sem mascara.
- **Risco se ignorarmos**: exposicao indevida de CPF completo em situacoes nao previstas ou mascaramento incorreto.

### MYS-008: Bloco legado comentado do Banco Real

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L213-L217`
- **O que esperávamos**: codigo morto removido ou documentado formalmente em ADR de descontinuacao.
- **O que o código faz**: mantem bloco comentado com logica de integracao bancaria antiga (Banco Real, adquirido em 2007).
- **Hipótese do time**: preservacao arqueologica por receio de perder conhecimento funcional ou por auditoria regulatoria.
- **Risco se ignorarmos**: confusao em manutencao futura e risco de reativacao acidental sem contexto de negocio.

### MYS-009: Desalinhamento entre imutabilidade de DDM e filtro de relatorio

- **Arquivo**: `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm#L9-L12`
- **O que esperávamos**: se DDM declara imutabilidade, relatorio deve exibir tudo sem filtro.
- **O que o código faz**: DDM afirma que auditoria nao permite update/delete, mas relatorio ainda filtra eventos antes de exibir.
- **Hipótese do time**: desalinhamento entre obrigacao legal (imutabilidade) e pratica operacional (filtro por conveniencia).
- **Risco se ignorarmos**: auditoria legal incompleta se filtro esconder eventos relevantes para investigacao.

### MYS-010: Inconsistencia de status entre DDM e relatorios

- **Arquivo**: `01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm#L54-L57`
- **O que esperávamos**: unificacao de codigo de status em toda a aplicacao.
- **O que o código faz**: DDM define `X=Cancelado`, mas relatorios operacionais usam `C` como cancelado.
- **Hipótese do time**: evolucao historica de convencao de status sem sincronizacao entre camadas.
- **Risco se ignorarmos**: mapeamento incorreto em APIs REST futuras, quebrando consistencia de dominio em bounded context Payment.

---

## Easter Eggs

> Dica: existem **3 easter eggs** escondidos no código legado. Registre aqui os que encontrar:

1. [x] Easter Egg 1: Comentario historico da integracao Banco Real (adquirido em 2007) mantido no fonte (`BATCHCON.NSN#L213-L217`).
2. [x] Easter Egg 2: Mensagem de regiao especial com atalho de elegibilidade (`VALELEG.NSN#L107-L111`).
3. [x] Easter Egg 3: Excecao `CPF 000...` descrita como teste governo (`VALBENEF.NSN#L195-L200`).

## Resumo

- Total de mistérios encontrados: 10
- Confiança alta: 5
- Confiança média: 5
- Confiança baixa: 0
- Easter eggs encontrados: 3 / 3

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-checklist.md"><strong>mysteries-checklist.md</strong></a><br/>
<sub>Lista do que procurar.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="discovery-report.md"><strong>discovery-report.md</strong></a><br/>
<sub>Síntese final.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

