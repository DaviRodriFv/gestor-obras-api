package gestor_obras_api.dto;

import gestor_obras_api.model.TipoCargo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private TipoCargo cargo;
    private String telefone;
    private Boolean ativo;
    private String token;
}
