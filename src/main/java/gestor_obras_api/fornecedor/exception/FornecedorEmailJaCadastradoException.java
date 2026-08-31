package gestor_obras_api.fornecedor.exception;

public class FornecedorEmailJaCadastradoException extends RuntimeException {

    public FornecedorEmailJaCadastradoException(String email) {
        super("Email já cadastrado para outro fornecedor: " + email);
    }
}
