package gestor_obras_api.cronograma.dto;

import gestor_obras_api.cronograma.model.StatusEtapa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EtapaRequestDTO {

    @NotBlank
    @Size(min = 3, max = 150)
    private String nome;

    private String descricao;

    @NotNull
    private LocalDate dataPrevistaInicio;

    @NotNull
    private LocalDate dataPrevistaFim;

    private StatusEtapa status;
}
