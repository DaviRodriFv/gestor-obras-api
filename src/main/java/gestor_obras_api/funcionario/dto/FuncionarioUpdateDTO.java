package gestor_obras_api.funcionario.dto;

import gestor_obras_api.funcionario.model.TipoCargo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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

    @Pattern(regexp = "\\(\\d{2}\\) \\d{4,5}-\\d{4}", message = "Telefone deve estar no formato (00) 00000-0000")
    private String telefone;

    private Boolean ativo;
}
