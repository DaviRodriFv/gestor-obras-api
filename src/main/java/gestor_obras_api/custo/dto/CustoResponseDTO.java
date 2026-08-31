package gestor_obras_api.custo.dto;

import gestor_obras_api.custo.model.CategoriaCusto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CustoResponseDTO {
    private UUID id;
    private UUID obraId;
    private CategoriaCusto categoria;
    private BigDecimal valor;
    private LocalDate data;
    private String descricao;
    private LocalDateTime criadoEm;
}
