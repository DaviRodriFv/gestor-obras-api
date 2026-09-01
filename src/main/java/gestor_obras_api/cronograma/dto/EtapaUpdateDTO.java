package gestor_obras_api.cronograma.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EtapaUpdateDTO {

    @Size(min = 3, max = 150)
    private String nome;

    private String descricao;

    private LocalDate dataPrevistaInicio;

    private LocalDate dataPrevistaFim;
}
