package gestor_obras_api.orcamento.exception;

import java.util.UUID;

public class OrcamentoNaoPertenceFornecedorException extends RuntimeException {

    public OrcamentoNaoPertenceFornecedorException(UUID orcamentoId, UUID fornecedorId) {
        super("Orçamento " + orcamentoId + " não pertence ao fornecedor " + fornecedorId);
    }
}
