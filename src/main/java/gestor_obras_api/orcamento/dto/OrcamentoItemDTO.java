package gestor_obras_api.orcamento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrcamentoItemDTO {

    @NotBlank
    private String descricaoMaterial;

    @NotNull
    @DecimalMin(value = "0.001")
    private BigDecimal quantidade;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal precoUnitario;
}
