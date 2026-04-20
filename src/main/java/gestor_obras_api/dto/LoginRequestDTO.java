package gestor_obras_api.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String senha;
}
