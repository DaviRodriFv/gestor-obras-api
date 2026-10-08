package gestor_obras_api.financeiro.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Teto de gasto da obra (Obra.orcamento). Não confundir com
 * orcamento.dto.OrcamentoRequestDTO, que é a proposta de um fornecedor.
 */
@Data
public class OrcamentoObraRequestDTO {

    @NotNull
    private BigDecimal valor;
}
