package gestor_obras_api.financeiro.controller;

import gestor_obras_api.financeiro.dto.DashboardFinanceiroResponseDTO;
import gestor_obras_api.financeiro.dto.OrcamentoObraRequestDTO;
import gestor_obras_api.financeiro.service.DashboardFinanceiroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/obras/{obraId}")
@RequiredArgsConstructor
public class DashboardFinanceiroController {

    private final DashboardFinanceiroService dashboardFinanceiroService;

    @GetMapping("/financeiro")
    public ResponseEntity<DashboardFinanceiroResponseDTO> gerarResumo(@PathVariable UUID obraId) {
        return ResponseEntity.ok(dashboardFinanceiroService.gerarResumo(obraId));
    }

    @PutMapping("/orcamento")
    public ResponseEntity<DashboardFinanceiroResponseDTO> definirOrcamento(
            @PathVariable UUID obraId,
            @Valid @RequestBody OrcamentoObraRequestDTO dto) {
        return ResponseEntity.ok(dashboardFinanceiroService.definirOrcamento(obraId, dto));
    }
}
