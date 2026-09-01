package gestor_obras_api.orcamento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrcamentoResponseDTO {
    private UUID id;
    private UUID fornecedorId;
    private String fornecedorNome;
    private UUID obraId;
    private String obraNome;
    private String descricao;
    private LocalDate dataOrcamento;
    private BigDecimal valorTotal;
    private List<OrcamentoItemResponseDTO> itens;
    private String arquivoNome;
    private String arquivoTipo;
    private boolean possuiArquivo;
    private LocalDateTime criadoEm;
}
