package gestor_obras_api.obra;

public enum StatusObra {
    EM_ANDAMENTO, PAUSADA, CONCLUIDA, CANCELADA;

    public boolean isTerminal() {
        return this == CONCLUIDA || this == CANCELADA;
    }
}
