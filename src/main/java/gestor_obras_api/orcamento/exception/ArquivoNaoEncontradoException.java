package gestor_obras_api.orcamento.exception;

import java.util.UUID;

public class ArquivoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ArquivoNaoEncontradoException(UUID orcamentoId) {
        super("Orçamento " + orcamentoId + " não possui arquivo anexado");
    }
}
