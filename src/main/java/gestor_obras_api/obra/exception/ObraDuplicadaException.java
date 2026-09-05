package gestor_obras_api.obra.exception;

public class ObraDuplicadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ObraDuplicadaException(String nome, String cliente) {
        super("Já existe uma obra com nome '" + nome + "' para o cliente '" + cliente + "'");
    }
}
