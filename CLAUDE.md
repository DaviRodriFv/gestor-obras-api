# Gestor de Obras — API

Backend do sistema de gestão de obras para a André Paulino Negócios Imobiliários. Spring Boot 4 (Java 21) + Spring Data JPA + Spring Security (JWT) + PostgreSQL + Lombok.

Frontend consumidor: repositório irmão `gestor-obras-app` (React + Vite + Tailwind).

## Comandos

Rodar a partir da raiz do projeto:
- `./mvnw spring-boot:run` — servidor de desenvolvimento
- `./mvnw clean package` — build
- `./mvnw test` — testes

## Módulos implementados

Pacote base `gestor_obras_api` (nome com underscore porque `gestor-obras-api` não é um identificador Java válido):
- `auth/` — login e emissão de JWT (`JwtService`, `JwtAuthFilter`, `SecurityConfig`)
- `funcionario/` — CRUD completo de funcionários
- `obra/` — CRUD de obras + workflow de status (`StatusObra`, transições em `ObraService`); `Obra.orcamento` é o teto de gasto
- `cronograma/` — etapas da obra (`/api/obras/{obraId}/cronograma`)
- `fornecedor/` + `orcamento/` — fornecedores, vínculo com obras e orçamentos (propostas) de fornecedor
- `custo/` — GerenciarCustos (`/api/obras/{obraId}/custos`): `StatusCusto` PENDENTE→PAGO|CANCELADO, `CustoRepository.somarPorCategoria` (sem cancelados). Anexo do lançamento (orçamento/nota, PDF/PNG/JPG) em `.../custos/{id}/anexo`: bytes na tabela `custo_anexo` (`CustoAnexo`), só nome/tipo no `Custo`, para a listagem não carregar arquivos
- `financeiro/` — DashboardFinanceiro (`/api/obras/{obraId}/financeiro`, `PUT .../orcamento`): `SituacaoFinanceira` calculada, não persistida
- `relatorio/` — Emitir Relatórios (`GET /api/obras/{obraId}/relatorio`): só agrega obra + cronograma + financeiro + custos via os services existentes, nada é persistido
- `dashboard/` — tela inicial (`GET /api/dashboard`): por obra, % de conclusão, próxima etapa e dias para o prazo (alerta ≤ 30 dias); contadores por status. Situação financeira só vem para `ADMINISTRADOR` (decidido no controller pelo cargo do usuário logado)

Todas as entidades usam soft delete (`@SQLDelete` + `@SQLRestriction("excluido_em IS NULL")`), exceto `OrcamentoItem` e `CustoAnexo`. Uma associação `@ManyToOne` para uma linha soft-deletada quebra ao carregar (`EntityNotFoundException`), então desvincule antes de excluir (ver `FornecedorService.deletar`). Custos, financeiro e relatório são exclusivos de `ADMINISTRADOR` (`SecurityConfig`). Schema via `ddl-auto=update` (sem Flyway).

## Documentação do estágio (TCC — Gestor de Obras para André Paulino Negócios Imobiliários)

Contexto do produto em `docs/estagio/` (copiado do repositório `gestor-obras-app`, que é a fonte original — ao atualizar uma spec, replicar dos dois lados):
- `especificacoes/` — versões em Markdown das specs (sempre carregadas via import abaixo)
- `originais/casos-de-uso/` — PDFs originais das especificações de caso de uso
- `originais/documentos/` — .docx originais (Documento de Visão, Especificação Complementar, Pedido do Investidor)
- `diagramas/` — imagens de diagramas (lidas sob demanda, não ficam sempre carregadas)

### Visão geral do produto (sempre carregado)
@docs/estagio/especificacoes/visao-do-produto.md
@docs/estagio/especificacoes/especificacao-complementar.md
@docs/estagio/especificacoes/pedido-investidor.md
@docs/estagio/especificacoes/diagrama-casos-de-uso.md

### Casos de uso especificados (sempre carregados)
@docs/estagio/especificacoes/casos-de-uso/gerenciar-obras.md
@docs/estagio/especificacoes/casos-de-uso/gerenciar-funcionarios.md
@docs/estagio/especificacoes/casos-de-uso/gerenciar-cronograma.md
@docs/estagio/especificacoes/casos-de-uso/gerenciar-fornecedores.md
@docs/estagio/especificacoes/casos-de-uso/emitir-relatorios.md

### Estado dos casos de uso no backend
GerenciarObras, GerenciarFuncionarios, GerenciarCronograma, GerenciarFornecedores, GerenciarCustos, DashboardFinanceiro e Emitir Relatórios estão implementados. Os prompts usados para Custos/Financeiro estão em `docs/estagio/mb-novos-casos-uso/`.

**Pendentes de especificação** (aparecem no diagrama de casos de uso, ainda sem `.md`): `DashboardFinanceiro` e `GerenciarCustos` (já com código). Ao especificá-los, criar o `.md` em `docs/estagio/especificacoes/casos-de-uso/` (nos dois repositórios) e adicionar o import aqui.
