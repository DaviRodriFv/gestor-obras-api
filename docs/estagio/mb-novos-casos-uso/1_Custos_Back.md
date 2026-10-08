Você vai implementar o **back-end** do caso de uso **GerenciarCustos** no **Gestor de Obras**, meu projeto de estágio (back-end Spring Boot + JPA + PostgreSQL, front-end React). Os casos de uso **GerenciarObras, GerenciarFuncionarios, GerenciarCronograma e GerenciarFornecedores já estão prontos**.

Esta é a **parte 1 de 4**. As próximas são: front-end de Custos, back-end do DashboardFinanceiro e front-end do DashboardFinanceiro. **Neste prompt, mexa só no back-end.**

A documentação (especificação de caso de uso, diagrama de sequência e diagrama de estado) já foi escrita. O código precisa bater com ela, inclusive nos nomes de classes e métodos listados abaixo.

---

## 1. Antes de escrever código, explore o back-end

Leia o código existente e replique os padrões que já estão em uso. Não crie padrões novos. Em especial, veja:

- Como Obra, Etapa (cronograma) e Fornecedor estão organizados: `Controller` → `Service` → `Repository`, DTOs de request/update/response, mapper (`ObraMapper` ou `toDTO` no service), exceções (`ObraNotFoundException`, `FornecedorNotFoundException`) e o handler global que as converte em 404/400/409.
- Se já existem a entidade `Custo` e o enum `CategoriaCusto` (o diagrama de classes prevê os dois). Se existirem, complete o que falta em vez de recriar.
- Como o banco evolui: Flyway, Liquibase ou `ddl-auto`. Siga o mesmo mecanismo.
- Como a autenticação e os perfis (`TipoCargo`: `ADMINISTRADOR` / `EQUIPE`) são aplicados.

Se algo do que descrevo abaixo conflitar com o código real (um nome diferente, um campo que já existe com outro tipo), **adapte ao que já existe** e me avise no resumo final.

---

## 2. Modelo de dados

**Obra**: adicionar `orcamento : BigDecimal` (nullable, precisão 15, escala 2), se ainda não existir. Neste prompt ele só é lido, no percentual por categoria. O endpoint para definir o orçamento vem na parte 3 (DashboardFinanceiro).

**Custo** (tabela `custo`):

| Campo | Tipo | Regra |
|---|---|---|
| id | UUID | gerado |
| obra | ManyToOne `Obra` | obrigatório |
| fornecedor | ManyToOne `Fornecedor` | **opcional** |
| categoria | `CategoriaCusto` (`MATERIAL`, `MAO_DE_OBRA`, `SERVICO_TERCEIRIZADO`) | obrigatório |
| valor | BigDecimal (15,2) | obrigatório, **> 0** |
| data | LocalDate | obrigatório |
| descricao | String (até 255) | obrigatório |
| status | `StatusCusto` (`PENDENTE`, `PAGO`, `CANCELADO`) | padrão `PENDENTE` |
| criadoEm | LocalDateTime | preenchido automaticamente |

**Integridade:**
- Ao excluir uma obra, os custos dela são excluídos junto. Use o mesmo tratamento que já é dado às etapas do cronograma.
- Ao excluir um fornecedor, os custos **devem ser preservados** (a especificação de GerenciarFornecedores exige isso): `custo.fornecedor` passa a `null`. Confira como `FornecedorService` exclui hoje e ajuste o que for necessário.

---

## 3. Endpoints

Classes: `CustoController`, `CustoService`, `CustoRepository`, `CustoRequestDTO`, `CustoUpdateDTO`, `CustoResponseDTO` (e mapper, se esse for o padrão).

| Método | Rota | Retorno |
|---|---|---|
| GET | `/api/obras/{obraId}/custos?categoria=&status=&busca=` | lista ordenada por `data` desc (`busca` filtra por descrição) |
| GET | `/api/obras/{obraId}/custos/{id}` | 200 |
| POST | `/api/obras/{obraId}/custos` | **201** |
| PUT | `/api/obras/{obraId}/custos/{id}` | 200 |
| PATCH | `/api/obras/{obraId}/custos/{id}/status` (body `{ "status": "PAGO" }`) | 200 |
| DELETE | `/api/obras/{obraId}/custos/{id}` | 204 |
| GET | `/api/obras/{obraId}/custos/por-categoria` | `[{ categoria, total, quantidade, percentualOrcamento }]` |

`CustoResponseDTO`: `id, obraId, categoria, valor, data, descricao, status, fornecedorId, fornecedorNome, criadoEm`.

---

## 4. Regras de negócio

**Criação.** Siga exatamente esta ordem, que é a do diagrama de sequência. Use `CustoService.criar(obraId, dto)`:
1. `obraRepository.findById(obraId)`. Se não existir: `ObraNotFoundException` → **404 "Obra não encontrada"**.
2. `validarLancamento(dto.valor, dto.status)`. Se `valor <= 0` ou `status == CANCELADO`: `IllegalArgumentException` → **400 "Dados do lançamento inválidos"**. Se `status` vier nulo, assuma `PENDENTE`. Na criação só são aceitos `PENDENTE` e `PAGO`.
3. Se `fornecedorId` foi informado: `fornecedorRepository.findById`. Se não existir: `FornecedorNotFoundException` → **404 "Fornecedor não encontrado"**.
4. Criar o `Custo`, `custoRepository.save(custo)`, `toDTO(custo)` → **201 Created**.

**Edição (PUT):** mesmas validações de valor e fornecedor. O status **não** muda pelo PUT, só pelo PATCH. Se o custo não pertencer à `obraId` da rota, responda **404 "Custo não encontrado"**.

**Status (PATCH):** só são permitidas as transições `PENDENTE → PAGO` e `PENDENTE → CANCELADO`. Qualquer outra → **400 "Transição de status inválida"**.

**Exclusão:** permitida em qualquer status.

**Por categoria:** retornar sempre as 3 categorias, com zero quando não houver lançamentos. Os custos `CANCELADO` ficam de fora. `percentualOrcamento = total / obra.orcamento × 100` (2 casas, `HALF_UP`), ou `null` se a obra não tiver orçamento. Implemente com uma query agrupada no repositório, `CustoRepository.somarPorCategoria(obraId)`. O DashboardFinanceiro (parte 3) vai reutilizar essa mesma query, então deixe o método público e bem nomeado.

---

## 5. Permissão

Os dados financeiros são restritos ao perfil administrador (o glossário diz que o perfil Equipe não tem acesso a dados financeiros). **Se o projeto já tiver autorização por perfil**, restrinja os endpoints de `/custos` a `ADMINISTRADOR`. **Se não tiver**, não crie infraestrutura nova de segurança; só me avise.

---

## 6. Testes e verificação

- Testes unitários de `CustoService` (siga a estrutura de testes existente; se não houver, use JUnit 5 + Mockito): obra inexistente; valor ≤ 0; criação com status `CANCELADO`; fornecedor inexistente; transições de status inválidas; custo de outra obra; cancelados fora do `por-categoria`.
- O back-end precisa compilar e passar em todos os testes (Maven/Gradle, conforme o projeto).
- Suba a aplicação e teste os endpoints com curl ou pelo cliente HTTP que o projeto usar: criar custos nas 3 categorias, editar um, pagar um, cancelar um, excluir outro, e conferir o `por-categoria`. Confira também que excluir um fornecedor mantém os custos dele.

**Não mexa no front-end** (é a parte 2). **Não faça commit nem push.** Deixe as alterações para eu revisar.

---

## 7. Resumo final que eu preciso

1. Arquivos criados e alterados.
2. Endpoints finais, com exemplos de request e response.
3. Migração criada, se houver.
4. Tudo o que ficou diferente do diagrama de classes atual, para eu atualizar no Astah. No mínimo, espero: `Obra.orcamento`, `Custo.status`, `Custo.fornecedor` e o enum `StatusCusto`.
5. Qualquer ponto em que você precisou adaptar este pedido ao código existente.
