package gestor_obras_api.fornecedor.exception;

import java.util.UUID;

public class FornecedorNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FornecedorNotFoundException(UUID id) {
        super("Fornecedor não encontrado com id: " + id);
    }
}
