package gestor_obras_api.funcionario.dto;

import gestor_obras_api.funcionario.model.TipoCargo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FuncionarioRequestDTO {

    @NotBlank
    @Size(min = 3, max = 100)
    private String nome;

    @NotBlank
    @Email
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

    @NotNull
    private TipoCargo cargo;

    private String role;

    @NotBlank
    private String telefone;
}
