# Plano de Execucao - S1 Arqueologia

## Objetivo

Executar a arqueologia do legado Natural/Adabas no Estagio 1, produzindo os 5 artefatos obrigatorios com rastreabilidade por arquivo e linha, prontos para a Passagem #1.

## Entradas obrigatorias

- [01-arqueologia/GUIDE.md](../../../01-arqueologia/GUIDE.md)
- [01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md](../../../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md)
- [01-arqueologia/legado-sifap/natural-programs](../../../01-arqueologia/legado-sifap/natural-programs)
- [01-arqueologia/legado-sifap/adabas-ddms](../../../01-arqueologia/legado-sifap/adabas-ddms)
- [01-arqueologia/legado-sifap/legacy-docs](../../../01-arqueologia/legado-sifap/legacy-docs)

## Artefatos de saida

- [01-arqueologia/glossary.md](../../../01-arqueologia/glossary.md)
- [01-arqueologia/business-rules-catalog.md](../../../01-arqueologia/business-rules-catalog.md)
- [01-arqueologia/dependency-map.md](../../../01-arqueologia/dependency-map.md)
- [01-arqueologia/mysteries-found.md](../../../01-arqueologia/mysteries-found.md)
- [01-arqueologia/discovery-report.md](../../../01-arqueologia/discovery-report.md)

## Criterios minimos (gate)

1. Glossario com 30+ termos e definicao objetiva.
2. Catalogo com 15+ regras BR e 100% com Programa Fonte preenchido.
3. Mapa Mermaid cobrindo os 15 programas Natural, sem orfaos.
4. Lista com 5+ misterios, cada um com evidencia e impacto.
5. Discovery report completo, sem placeholders.

## Sequencia operacional

1. Preparacao e contrato de qualidade.
- Confirmar DoR e atribuicao de 3 programas por par.
- Confirmar agente de estagio arqueologia selecionado.

2. Inventario tecnico do legado.
- Indexar os 15 arquivos NSN e os 4 DDMs.
- Levantar estruturas relevantes: IF, CALLNAT, COMPUTE, READ, FIND, STORE, UPDATE.

3. Extracao estruturada em paralelo.
- Trilha A (Natural): termos, regras candidatas, dependencias e condicoes especiais.
- Trilha B (DDM): campos, tipo/tamanho, marcas MU/PE e vinculacao com regras.

4. Preenchimento inicial dos 5 artefatos.
- Priorizar completude e rastreabilidade ja na primeira versao.

5. Consolidacao por artefato.
- Dedupe de termos e regras.
- Classificacao de risco das regras.
- Confirmacao de cobertura dos 15 programas no mapa.
- Consolidacao dos misterios com evidencia valida.
- Priorizacao de 5 a 8 regras criticas no discovery report.

6. Validacao de gate.
- Rodar checklist final e corrigir falhas ate 100% de conformidade.

7. Handoff da Passagem #1.
- Entregar pacote de arqueologia para o Estagio 2.

## Matriz de verificacao

1. Cobertura de fontes: 15 NSN e 4 DDM processados.
2. Programa Fonte em 100% das linhas do catalogo.
3. Referencias validas para arquivo e faixa de linha.
4. Sem placeholders em nenhum artefato final.

## Decisoes

- Escopo inclui apenas Estagio 1.
- Rastreabilidade por linha e criterio de aceite obrigatorio.
- Se qualquer gate falhar, nao avancar para Estagio 2.

## Responsavel e status

- Responsavel: Par 1 (Visao), com contribuicao de todos os pares.
- Status: Concluido.
- Ultima atualizacao: 2026-05-27.
