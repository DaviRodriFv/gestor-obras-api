package gestor_obras_api.custo.dto;

import gestor_obras_api.custo.model.CategoriaCusto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CustoRequestDTO {

    @NotNull
    private UUID obraId;

    @NotNull
    private CategoriaCusto categoria;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal valor;

    @NotNull
    private LocalDate data;

    private String descricao;
}
