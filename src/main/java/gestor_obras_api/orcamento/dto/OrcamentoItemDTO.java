package gestor_obras_api.orcamento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
    @Digits(integer = 9, fraction = 3, message = "Quantidade deve ter no máximo 3 casas decimais")
    private BigDecimal quantidade;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 10, fraction = 2, message = "Valor deve ter no máximo 2 casas decimais")
    private BigDecimal precoUnitario;
}
