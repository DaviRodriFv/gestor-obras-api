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
- `obra/` — CRUD de obras + workflow de status (`StatusObra`, transições em `ObraService`)

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

### Próximos casos de uso a implementar no backend
Já têm especificação pronta em `casos-de-uso/`, mas ainda não têm código em `src/main/java/gestor_obras_api/`:
- **GerenciarCronograma** — sem pacote próprio ainda; seguir o padrão de `obra/` (model → repository → service → controller, DTOs de request/response)
- **GerenciarFornecedores** — sem pacote próprio ainda; mesmo padrão

**Pendentes de especificação** (aparecem no diagrama de casos de uso, ainda sem `.md`): `DashboardFinanceiro`, `GerenciarCustos`, `Emitir Relatórios`. Ao especificá-los, criar o `.md` em `docs/estagio/especificacoes/casos-de-uso/` (nos dois repositórios) e adicionar o import aqui.
