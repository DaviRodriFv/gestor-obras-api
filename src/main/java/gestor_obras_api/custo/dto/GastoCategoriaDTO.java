package gestor_obras_api.custo.dto;

import gestor_obras_api.custo.model.CategoriaCusto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/** Usado por /custos/por-categoria e pelo DashboardFinanceiro. */
@Data
@AllArgsConstructor
public class GastoCategoriaDTO {
    private CategoriaCusto categoria;
    private BigDecimal total;
    private long quantidade;
    /** Nulo quando a obra não tem orçamento definido. */
    private BigDecimal percentualOrcamento;
}
