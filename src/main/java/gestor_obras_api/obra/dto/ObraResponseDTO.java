package gestor_obras_api.obra.dto;

import gestor_obras_api.obra.StatusObra;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObraResponseDTO {

    private UUID id;
    private Long usuarioId;
    private String nome;
    private String endereco;
    private String cliente;
    private LocalDate dataInicio;
    private LocalDate prazoConclusao;
    private StatusObra status;
    private LocalDateTime criadoEm;
}
