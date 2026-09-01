package gestor_obras_api.funcionario.dto;

import gestor_obras_api.funcionario.model.TipoCargo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FuncionarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private TipoCargo cargo;
    private String telefone;
    private Boolean ativo;
}
