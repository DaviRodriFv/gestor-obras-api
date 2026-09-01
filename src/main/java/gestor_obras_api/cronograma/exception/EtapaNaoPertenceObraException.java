package gestor_obras_api.cronograma.exception;

import java.util.UUID;

public class EtapaNaoPertenceObraException extends RuntimeException {

    public EtapaNaoPertenceObraException(UUID etapaId, UUID obraId) {
        super("Etapa " + etapaId + " não pertence à obra " + obraId);
    }
}
