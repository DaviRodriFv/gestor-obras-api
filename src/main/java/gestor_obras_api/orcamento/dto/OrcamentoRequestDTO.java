package gestor_obras_api.orcamento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class OrcamentoRequestDTO {

    @NotNull
    private UUID obraId;

    private String descricao;

    @NotNull
    private LocalDate dataOrcamento;

    @DecimalMin(value = "0.01")
    private BigDecimal valorTotal;

    @Valid
    private List<OrcamentoItemDTO> itens;
}
