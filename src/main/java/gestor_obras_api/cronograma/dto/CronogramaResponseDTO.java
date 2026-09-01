package gestor_obras_api.cronograma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CronogramaResponseDTO {
    private UUID obraId;
    private Integer progressoGeral;
    private List<EtapaResponseDTO> etapas;
}
