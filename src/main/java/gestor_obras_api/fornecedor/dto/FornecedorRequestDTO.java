package gestor_obras_api.fornecedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FornecedorRequestDTO {

    @NotBlank
    @Size(min = 3, max = 150)
    private String nome;

    @NotBlank
    @Size(max = 255)
    private String tipoServico;

    @NotBlank
    @Pattern(regexp = "\\(\\d{2}\\) \\d{4,5}-\\d{4}", message = "Telefone deve estar no formato (00) 00000-0000")
    private String telefone;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 255)
    private String endereco;
}
