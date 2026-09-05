package gestor_obras_api.funcionario.exception;

public class FuncionarioNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FuncionarioNotFoundException(Long id) {
        super("Funcionário não encontrado com id: " + id);
    }
}
