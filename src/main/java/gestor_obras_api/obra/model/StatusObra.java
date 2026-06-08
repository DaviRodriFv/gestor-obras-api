package gestor_obras_api.obra.model;

public enum StatusObra {
    EM_ANDAMENTO, PAUSADA, CONCLUIDA, CANCELADA;

    public boolean isTerminal() {
        return this == CONCLUIDA || this == CANCELADA;
    }
}
