package gestor_obras_api.cronograma.dto;

import gestor_obras_api.cronograma.model.StatusEtapa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class EtapaResponseDTO {
    private UUID id;
    private UUID obraId;
    private String nome;
    private String descricao;
    private LocalDate dataPrevistaInicio;
    private LocalDate dataPrevistaFim;
    private LocalDate dataRealInicio;
    private LocalDate dataRealFim;
    private Integer percentualProgresso;
    private StatusEtapa status;
    private LocalDateTime criadoEm;
}
