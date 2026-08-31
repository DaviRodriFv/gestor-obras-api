package gestor_obras_api.fornecedor.exception;

import java.util.UUID;

public class FornecedorNotFoundException extends RuntimeException {

    public FornecedorNotFoundException(UUID id) {
        super("Fornecedor não encontrado com id: " + id);
    }
}
