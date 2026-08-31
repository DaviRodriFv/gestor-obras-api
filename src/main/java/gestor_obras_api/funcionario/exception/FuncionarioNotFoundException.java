package gestor_obras_api.funcionario.exception;

public class FuncionarioNotFoundException extends RuntimeException {

    public FuncionarioNotFoundException(Long id) {
        super("Funcionário não encontrado com id: " + id);
    }
}
