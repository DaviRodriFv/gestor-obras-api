package gestor_obras_api.orcamento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class OrcamentoUpdateDTO {

    private UUID obraId;

    private String descricao;

    private LocalDate dataOrcamento;

    @DecimalMin(value = "0.01")
    private BigDecimal valorTotal;

    @Valid
    private List<OrcamentoItemDTO> itens;
}
