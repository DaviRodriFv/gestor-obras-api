package gestor_obras_api.custo.exception;

import java.util.UUID;

public class CustoNotFoundException extends RuntimeException {

    public CustoNotFoundException(UUID id) {
        super("Custo não encontrado com id: " + id);
    }
}
