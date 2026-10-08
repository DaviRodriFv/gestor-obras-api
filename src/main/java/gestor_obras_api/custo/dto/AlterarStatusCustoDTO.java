package gestor_obras_api.custo.dto;

import gestor_obras_api.custo.model.StatusCusto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlterarStatusCustoDTO {

    @NotNull
    private StatusCusto status;
}
