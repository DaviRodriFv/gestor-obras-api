package gestor_obras_api.fornecedor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ObraResumoDTO {
    private UUID id;
    private String nome;
}
