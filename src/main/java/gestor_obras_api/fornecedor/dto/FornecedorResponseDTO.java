package gestor_obras_api.fornecedor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class FornecedorResponseDTO {
    private UUID id;
    private String nome;
    private String tipoServico;
    private String telefone;
    private String email;
    private String endereco;
    private LocalDateTime criadoEm;
    private Boolean ativo;
    private List<ObraResumoDTO> obrasVinculadas;
}
