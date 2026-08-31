package gestor_obras_api.funcionario.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("Email já cadastrado: " + email);
    }
}
