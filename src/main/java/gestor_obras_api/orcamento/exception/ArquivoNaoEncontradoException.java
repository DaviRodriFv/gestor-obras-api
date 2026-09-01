package gestor_obras_api.orcamento.exception;

import java.util.UUID;

public class ArquivoNaoEncontradoException extends RuntimeException {

    public ArquivoNaoEncontradoException(UUID orcamentoId) {
        super("Orçamento " + orcamentoId + " não possui arquivo anexado");
    }
}
