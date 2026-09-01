package gestor_obras_api.cronograma.exception;

import java.util.UUID;

public class EtapaNotFoundException extends RuntimeException {

    public EtapaNotFoundException(UUID id) {
        super("Etapa não encontrada com id: " + id);
    }
}
