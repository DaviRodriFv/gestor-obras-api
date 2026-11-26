package gestor_obras_api.orcamento.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class OrcamentoUpdateDTO {

    private UUID obraId;

    @Size(max = 255)
    private String descricao;

    private LocalDate dataOrcamento;

    @DecimalMin(value = "0.01")
    @Digits(integer = 10, fraction = 2, message = "Valor deve ter no máximo 2 casas decimais")
    private BigDecimal valorTotal;

    @Valid
    private List<OrcamentoItemDTO> itens;
}
