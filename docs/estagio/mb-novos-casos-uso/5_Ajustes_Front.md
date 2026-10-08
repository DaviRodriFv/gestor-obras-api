Você vai fazer **ajustes pequenos no front-end** do **Gestor de Obras** (React + Vite + Tailwind, repositório `gestor-obras-app`), para alinhar as telas **Custos** e **Financeiro** ao back-end que acabou de ser finalizado. **Não mexa no back-end.** **Não faça commit nem push.**

As telas já existem (`src/components/custos/`, `src/components/financeiro/`, `src/services/custosService.js`, `src/services/financeiroService.js`) e as rotas da API já batem. Só os pontos abaixo divergem.

---

## 1. Modal de lançamento: status não muda pela edição

No back-end, `PUT /api/obras/{obraId}/custos/{id}` **ignora o campo `status`**. O status só muda por `PATCH /api/obras/{obraId}/custos/{id}/status`, e só são permitidas as transições:

- `PENDENTE → PAGO` (Registrar pagamento)
- `PENDENTE → CANCELADO` (Cancelar lançamento)

Qualquer outra transição responde **400**.

Hoje o `LancamentoFormModal.jsx` mostra o select de status no modo edição (com `CANCELADO` incluído) e manda `status` no PUT. O usuário muda o status, salva, e nada acontece.

**Ajuste:**
- No modo edição, **esconda o select de status** (ou mostre o status atual só como texto/badge, sem edição).
- Na criação, mantenha o select com `Pendente` e `Pago`.
- No modo edição, não envie `status` no body do PUT.
- As ações "Registrar pagamento" e "Cancelar lançamento" do modal de detalhes já usam o PATCH. Mantenha como está.

## 2. Edição envia o lançamento completo

O PUT substitui todos os dados do lançamento: `categoria`, `valor`, `data` e `descricao` são obrigatórios. `fornecedorId: null` **remove** o fornecedor do lançamento. O modal já envia todos esses campos, então confira apenas que, ao limpar o fornecedor no select, vai `null` (e não string vazia).

## 3. Perfil EQUIPE recebe 403

`/api/obras/{id}/custos/**`, `/api/obras/{id}/financeiro` e `/api/obras/{id}/orcamento` agora são **exclusivos de ADMINISTRADOR**. O perfil EQUIPE recebe **403**.

- Se o menu (`Navbar.jsx`) já esconde itens por perfil, esconda **Custos** e **Financeiro** para `EQUIPE`.
- Se alguém da EQUIPE cair na rota pela URL, a tela deve mostrar a mensagem de erro da API do mesmo jeito que as outras telas mostram, sem quebrar.

## 4. Mensagens de erro da API

O front já usa `err.response.data.message`. As mensagens que podem aparecer:

| Situação | Status | `message` |
|---|---|---|
| Valor ≤ 0, ou criar com status CANCELADO | 400 | `Dados do lançamento inválidos` |
| Transição de status inválida | 400 | `Transição de status inválida: PAGO -> CANCELADO` |
| Orçamento ≤ 0 | 400 | `Orçamento deve ser maior que zero` |
| Obra, custo ou fornecedor inexistente | 404 | `Obra não encontrada com id: ...` (idem para custo/fornecedor) |
| Campo obrigatório faltando | 400 | `Erro de validação` + objeto `campos` |

Nada a mudar se a tela já exibe `message`. Só confira que os textos aparecem bem.

## 5. Fornecedor excluído

Quando um fornecedor é excluído, os lançamentos dele **continuam existindo**, mas ficam sem fornecedor (`fornecedorId` e `fornecedorNome` passam a `null`). A tabela deve mostrar "—" nesse caso.

## 6. Atualizar o HANDOFF_FRONTEND.md

O `HANDOFF_FRONTEND.md` (raiz do `gestor-obras-app`) está desatualizado:
- A seção **8. Custos** ainda descreve `/api/custos` (rota antiga, removida). Troque pela tabela de rotas aninhadas em `/api/obras/{obraId}/custos` (as do `2_Custos_Front.md`), com o `PATCH /status` e o `GET /por-categoria`.
- A seção **9. Financeiro** descreve o stub `/api/financeiro/dashboard` e `/api/financeiro/entrada`, que **foram removidos**. Troque por `GET /api/obras/{obraId}/financeiro` e `PUT /api/obras/{obraId}/orcamento` (as do `4_Financeiro_Front.md`).
- Na tabela de permissões, `custos` e `financeiro` passam a ser só ADMINISTRADOR.

---

## Verificação

- `npm run build` (e lint, se existir) passando.
- Teste manual com o back rodando:
  1. Editar um lançamento: o status não aparece como editável, e o PUT funciona.
  2. Registrar pagamento e cancelar pelo modal de detalhes.
  3. Logar como `equipe@gestor.com` / `equipe@1234` e confirmar que Custos e Financeiro não aparecem no menu (ou mostram o erro de permissão).

## Resumo final que eu preciso

1. Arquivos alterados.
2. Qualquer ponto em que precisou adaptar este pedido ao código existente.
