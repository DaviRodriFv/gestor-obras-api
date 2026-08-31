package gestor_obras_api.custo.dto;

import gestor_obras_api.custo.model.CategoriaCusto;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustoUpdateDTO {

    private CategoriaCusto categoria;

    @DecimalMin(value = "0.01")
    private BigDecimal valor;

    private LocalDate data;

    private String descricao;
}
