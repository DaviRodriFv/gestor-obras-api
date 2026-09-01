package gestor_obras_api.cronograma.dto;

import gestor_obras_api.cronograma.model.StatusEtapa;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AtualizarProgressoDTO {

    private LocalDate dataRealInicio;

    private LocalDate dataRealFim;

    @Min(0)
    @Max(100)
    private Integer percentualProgresso;

    private StatusEtapa status;
}
