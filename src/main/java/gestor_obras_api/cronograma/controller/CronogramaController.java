package gestor_obras_api.cronograma.controller;

import gestor_obras_api.cronograma.dto.AtualizarProgressoDTO;
import gestor_obras_api.cronograma.dto.CronogramaResponseDTO;
import gestor_obras_api.cronograma.dto.EtapaRequestDTO;
import gestor_obras_api.cronograma.dto.EtapaResponseDTO;
import gestor_obras_api.cronograma.dto.EtapaUpdateDTO;
import gestor_obras_api.cronograma.service.EtapaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/obras/{obraId}/cronograma")
@RequiredArgsConstructor
public class CronogramaController {

    private final EtapaService etapaService;

    @GetMapping
    public ResponseEntity<CronogramaResponseDTO> buscarCronograma(@PathVariable UUID obraId) {
        return ResponseEntity.ok(etapaService.buscarCronograma(obraId));
    }

    @GetMapping("/etapas/{etapaId}")
    public ResponseEntity<EtapaResponseDTO> buscarEtapa(
            @PathVariable UUID obraId,
            @PathVariable UUID etapaId) {
        return ResponseEntity.ok(etapaService.buscarPorId(obraId, etapaId));
    }

    @PostMapping("/etapas")
    public ResponseEntity<EtapaResponseDTO> criarEtapa(
            @PathVariable UUID obraId,
            @Valid @RequestBody EtapaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(etapaService.criar(obraId, dto));
    }

    @PutMapping("/etapas/{etapaId}")
    public ResponseEntity<EtapaResponseDTO> atualizarEtapa(
            @PathVariable UUID obraId,
            @PathVariable UUID etapaId,
            @Valid @RequestBody EtapaUpdateDTO dto) {
        return ResponseEntity.ok(etapaService.atualizar(obraId, etapaId, dto));
    }

    @PatchMapping("/etapas/{etapaId}/progresso")
    public ResponseEntity<EtapaResponseDTO> atualizarProgresso(
            @PathVariable UUID obraId,
            @PathVariable UUID etapaId,
            @Valid @RequestBody AtualizarProgressoDTO dto) {
        return ResponseEntity.ok(etapaService.atualizarProgresso(obraId, etapaId, dto));
    }

    @DeleteMapping("/etapas/{etapaId}")
    public ResponseEntity<Void> deletarEtapa(
            @PathVariable UUID obraId,
            @PathVariable UUID etapaId) {
        etapaService.deletar(obraId, etapaId);
        return ResponseEntity.noContent().build();
    }
}
