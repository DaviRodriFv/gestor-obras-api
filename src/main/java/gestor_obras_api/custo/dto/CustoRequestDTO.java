package gestor_obras_api.custo.dto;

import gestor_obras_api.custo.model.CategoriaCusto;
import gestor_obras_api.custo.model.StatusCusto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A regra de valor > 0 e de status inicial fica em CustoService.validarLancamento,
 * para responder com a mensagem definida na especificação.
 */
@Data
public class CustoRequestDTO {

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

    /** PENDENTE ou PAGO. Nulo assume PENDENTE. */
    private StatusCusto status;
}
