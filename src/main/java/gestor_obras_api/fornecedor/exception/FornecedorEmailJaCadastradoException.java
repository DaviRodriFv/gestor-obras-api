package gestor_obras_api.fornecedor.exception;

public class FornecedorEmailJaCadastradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FornecedorEmailJaCadastradoException(String email) {
        super("Email já cadastrado para outro fornecedor: " + email);
    }
}
