package gestor_obras_api.dto;

import gestor_obras_api.obra.StatusObra;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlterarStatusDTO {

    @NotNull
    private StatusObra novoStatus;
}
