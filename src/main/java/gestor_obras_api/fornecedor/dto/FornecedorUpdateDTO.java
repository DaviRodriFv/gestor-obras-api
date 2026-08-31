package gestor_obras_api.fornecedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FornecedorUpdateDTO {

    @Size(min = 3, max = 150)
    private String nome;

    private String tipoServico;

    private String telefone;

    @Email
    private String email;

    private String endereco;

    private Boolean ativo;
}
