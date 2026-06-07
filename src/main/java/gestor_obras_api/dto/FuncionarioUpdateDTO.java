package gestor_obras_api.dto;

import gestor_obras_api.model.TipoCargo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FuncionarioUpdateDTO {

    @Size(min = 3, max = 100)
    private String nome;

    @Email
    private String email;

    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

    private TipoCargo cargo;

    private String telefone;

    private Boolean ativo;
}
