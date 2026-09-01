package gestor_obras_api.orcamento.exception;

import java.util.UUID;

public class OrcamentoNotFoundException extends RuntimeException {

    public OrcamentoNotFoundException(UUID id) {
        super("Orçamento não encontrado com id: " + id);
    }
}
