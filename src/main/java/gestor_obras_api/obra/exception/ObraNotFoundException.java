package gestor_obras_api.obra.exception;

import java.util.UUID;

public class ObraNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ObraNotFoundException(UUID id) {
        super("Obra não encontrada com id: " + id);
    }
}
