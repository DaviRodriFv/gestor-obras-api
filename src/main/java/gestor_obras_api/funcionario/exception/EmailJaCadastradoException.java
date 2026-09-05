package gestor_obras_api.funcionario.exception;

public class EmailJaCadastradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmailJaCadastradoException(String email) {
        super("Email já cadastrado: " + email);
    }
}
