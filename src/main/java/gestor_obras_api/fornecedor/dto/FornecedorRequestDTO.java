package gestor_obras_api.fornecedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FornecedorRequestDTO {

    @NotBlank
    @Size(min = 3, max = 150)
    private String nome;

    @NotBlank
    private String tipoServico;

    @NotBlank
    private String telefone;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String endereco;
}
