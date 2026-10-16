package gestor_obras_api.custo.exception;

import java.util.UUID;

public class AnexoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AnexoNaoEncontradoException(UUID custoId) {
        super("Custo " + custoId + " não possui arquivo anexado");
    }
}
