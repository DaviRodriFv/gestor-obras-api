package gestor_obras_api.obra.exception;

import gestor_obras_api.obra.model.StatusObra;

public class TransicaoStatusInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TransicaoStatusInvalidaException(StatusObra de, StatusObra para) {
        super("Transição de status inválida: " + de + " -> " + para);
    }

    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}
