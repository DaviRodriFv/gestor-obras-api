package gestor_obras_api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String role;
    private String telefone;
    private Boolean ativo;
    private String token;
}
