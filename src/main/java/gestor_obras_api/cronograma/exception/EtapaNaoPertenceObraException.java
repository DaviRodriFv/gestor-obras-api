package gestor_obras_api.cronograma.exception;

import java.util.UUID;

public class EtapaNaoPertenceObraException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EtapaNaoPertenceObraException(UUID etapaId, UUID obraId) {
        super("Etapa " + etapaId + " não pertence à obra " + obraId);
    }
}
