package gestor_obras_api.custo.dto;

import gestor_obras_api.custo.model.CategoriaCusto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Substitui os dados do lançamento. O status não muda por aqui, só pelo PATCH /status.
 * fornecedorId nulo remove o vínculo com o fornecedor.
 */
@Data
public class CustoUpdateDTO {

    @NotNull
    private CategoriaCusto categoria;

    @NotNull
    private BigDecimal valor;

    @NotNull
    private LocalDate data;

    @NotBlank
    @Size(max = 255)
    private String descricao;

    private UUID fornecedorId;
}
