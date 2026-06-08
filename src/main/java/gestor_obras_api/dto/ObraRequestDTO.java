package gestor_obras_api.dto;

import gestor_obras_api.obra.StatusObra;
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
public class ObraRequestDTO {

    @NotBlank
    @Size(min = 3, max = 150)
    private String nome;

    @NotBlank
    private String endereco;

    @NotBlank
    private String cliente;

    @NotNull
    private LocalDate dataInicio;

    @NotNull
    private LocalDate prazoConclusao;

    private StatusObra status;
}
