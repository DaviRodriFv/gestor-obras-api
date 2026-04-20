package gestor_obras_api.dto;

import gestor_obras_api.model.TipoCargo;
import lombok.Data;

@Data
public class FuncionarioRequestDTO {
    private String nome;
    private String email;
    private String senha;
    private TipoCargo cargo;
    private String telefone;
    private Boolean ativo;
}
