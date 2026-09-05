package gestor_obras_api.orcamento.exception;

import java.util.UUID;

public class OrcamentoNaoPertenceFornecedorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrcamentoNaoPertenceFornecedorException(UUID orcamentoId, UUID fornecedorId) {
        super("Orçamento " + orcamentoId + " não pertence ao fornecedor " + fornecedorId);
    }
}
