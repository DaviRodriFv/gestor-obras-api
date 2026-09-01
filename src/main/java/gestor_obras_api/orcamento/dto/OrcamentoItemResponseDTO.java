package gestor_obras_api.orcamento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrcamentoItemResponseDTO {
    private UUID id;
    private String descricaoMaterial;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
}
