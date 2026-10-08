package gestor_obras_api.financeiro.model;

/** Calculada a partir de orçamento e total gasto; não é persistida. */
public enum SituacaoFinanceira {
    SEM_ORCAMENTO, DENTRO_DO_ORCAMENTO, EM_ALERTA, ORCAMENTO_EXCEDIDO
}
