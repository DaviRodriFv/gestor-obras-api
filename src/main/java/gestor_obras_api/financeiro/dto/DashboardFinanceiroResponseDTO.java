package gestor_obras_api.financeiro.dto;

import gestor_obras_api.custo.dto.GastoCategoriaDTO;
import gestor_obras_api.financeiro.model.SituacaoFinanceira;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DashboardFinanceiroResponseDTO {
    private UUID obraId;
    private String obraNome;
    private BigDecimal orcamento;
    private BigDecimal totalGasto;
    /** Nulo sem orçamento. Pode ser negativo quando o orçamento foi excedido. */
    private BigDecimal saldoRestante;
    /** Nulo sem orçamento. */
    private BigDecimal percentualConsumido;
    private SituacaoFinanceira situacao;
    private List<GastoCategoriaDTO> gastosPorCategoria;
}
