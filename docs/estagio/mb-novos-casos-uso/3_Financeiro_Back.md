Você vai implementar o **back-end** do caso de uso **DashboardFinanceiro** no **Gestor de Obras**, meu projeto de estágio (back-end Spring Boot + JPA + PostgreSQL, front-end React). Os casos de uso **GerenciarObras, GerenciarFuncionarios, GerenciarCronograma, GerenciarFornecedores e GerenciarCustos já estão prontos**.

Esta é a **parte 3 de 4**. As partes 1 e 2 (back-end e front-end de Custos) já foram feitas. Depois desta vem o front-end do DashboardFinanceiro. **Neste prompt, mexa só no back-end.**

A documentação (especificação de caso de uso, diagrama de sequência e diagrama de estado) já foi escrita. O código precisa bater com ela, inclusive nos nomes de classes e métodos listados abaixo.

---

## 1. Pré-requisito: confira o que a parte 1 entregou

Antes de começar, confirme que existem no back-end:
- O campo `Obra.orcamento : BigDecimal` (nullable).
- A entidade `Custo`, com `status : StatusCusto` (`PENDENTE`, `PAGO`, `CANCELADO`) e `categoria : CategoriaCusto` (`MATERIAL`, `MAO_DE_OBRA`, `SERVICO_TERCEIRIZADO`).
- O método `CustoRepository.somarPorCategoria(obraId)`, que soma os custos por categoria **sem os cancelados**.

Se `Obra.orcamento` não existir, crie, seguindo o mecanismo de migração do projeto. Se `Custo` ou `somarPorCategoria` não existirem, **pare e me avise**.

---

## 2. Explore o back-end antes de escrever código

Replique os padrões que já estão em uso (veja `ObraController`/`ObraService`, `CustoController`/`CustoService`): camadas, DTOs, mapper, exceções (`ObraNotFoundException`) e o handler global. Não crie padrões novos. Se algo do que descrevo abaixo conflitar com o código real, **adapte ao que já existe** e me avise no resumo final.

---

## 3. Modelo

Crie o **enum** `SituacaoFinanceira`: `SEM_ORCAMENTO`, `DENTRO_DO_ORCAMENTO`, `EM_ALERTA`, `ORCAMENTO_EXCEDIDO`. É calculado e **não é persistido**.

---

## 4. Endpoints

Classes: `DashboardFinanceiroController`, `DashboardFinanceiroService`, `DashboardFinanceiroResponseDTO`, `GastoCategoriaDTO`, `OrcamentoRequestDTO`.

| Método | Rota | Retorno |
|---|---|---|
| GET | `/api/obras/{obraId}/financeiro` | `DashboardFinanceiroResponseDTO` |
| PUT | `/api/obras/{obraId}/orcamento` (body `{ "valor": 150000.00 }`) | dashboard recalculado |

`DashboardFinanceiroResponseDTO`: `obraId, obraNome, orcamento, totalGasto, saldoRestante, percentualConsumido, situacao, gastosPorCategoria[]`.
`GastoCategoriaDTO`: `categoria, total, quantidade, percentualOrcamento`. Use o mesmo formato que `/custos/por-categoria` já retorna; se já existir um DTO equivalente, reutilize.

---

## 5. Regras de negócio

**Fluxo do GET** (igual ao diagrama de sequência), em `DashboardFinanceiroService.gerarResumo(obraId)`:
1. `obraRepository.findById`. Se não existir: **404 "Obra não encontrada"**.
2. `custoRepository.somarPorCategoria(obraId)`.
3. `calcularTotalGasto(gastosPorCategoria)`: soma dos custos `PENDENTE` + `PAGO`. Os cancelados não entram.
4. Se a obra **não tem orçamento**: `saldoRestante = null`, `percentualConsumido = null`, `situacao = SEM_ORCAMENTO`.
   Se **tem**: `calcularSaldoEPercentual(orcamento, totalGasto)`, com `saldo = orcamento − totalGasto` (pode ficar negativo) e `percentual = totalGasto / orcamento × 100` (2 casas, `HALF_UP`). Depois, `calcularSituacao(orcamento, totalGasto)`:
   - `totalGasto > orcamento` → `ORCAMENTO_EXCEDIDO`
   - `percentual >= 80` → `EM_ALERTA`. Use uma constante `LIMITE_ALERTA = 80`.
   - caso contrário → `DENTRO_DO_ORCAMENTO`
5. `toDTO(obra, indicadores)` → **200 OK**.

**PUT orçamento:** `valor > 0`, senão **400 "Orçamento deve ser maior que zero"**. Se a obra não existir, **404 "Obra não encontrada"**. Salva em `Obra.orcamento` e retorna o resultado de `gerarResumo(obraId)`.

---

## 6. Permissão

Os dados financeiros são restritos ao perfil administrador (o glossário diz que o perfil Equipe não tem acesso a dados financeiros). **Se o projeto já tiver autorização por perfil**, restrinja `/financeiro` e `/orcamento` a `ADMINISTRADOR`, do mesmo jeito que foi feito com `/custos`. **Se não tiver**, não crie infraestrutura nova de segurança; só me avise.

---

## 7. Testes e verificação

- Testes unitários de `DashboardFinanceiroService` (siga a estrutura de testes existente):
  - obra inexistente
  - sem orçamento (`SEM_ORCAMENTO`, saldo e percentual nulos)
  - percentual < 80 (`DENTRO_DO_ORCAMENTO`)
  - exatamente 80 (`EM_ALERTA`)
  - exatamente 100 (`EM_ALERTA`)
  - acima de 100 (`ORCAMENTO_EXCEDIDO`, saldo negativo)
  - custos cancelados fora do total
  - PUT com valor ≤ 0
- O back-end precisa compilar e passar em todos os testes, inclusive os que já existiam.
- Suba a aplicação e teste com curl: consultar o financeiro de uma obra sem orçamento, definir o orçamento, lançar custos até passar de 80% e depois de 100%, e conferir a `situacao` a cada passo.

**Não mexa no front-end** (é a parte 4). **Não faça commit nem push.** Deixe as alterações para eu revisar.

---

## 8. Resumo final que eu preciso

1. Arquivos criados e alterados.
2. Endpoints finais, com exemplos de request e response.
3. Migração criada, se houver.
4. Tudo o que ficou diferente do diagrama de classes atual, para eu atualizar no Astah. No mínimo, espero o enum `SituacaoFinanceira` (e `Obra.orcamento`, se precisou ser criado aqui).
5. Qualquer ponto em que você precisou adaptar este pedido ao código existente.
