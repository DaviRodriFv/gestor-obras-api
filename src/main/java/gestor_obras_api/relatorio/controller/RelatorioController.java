package gestor_obras_api.relatorio.controller;

import gestor_obras_api.funcionario.model.Funcionario;
import gestor_obras_api.relatorio.dto.RelatorioObraResponseDTO;
import gestor_obras_api.relatorio.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/obras/{obraId}")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/relatorio")
    public ResponseEntity<RelatorioObraResponseDTO> gerarRelatorioObra(
            @PathVariable UUID obraId,
            @AuthenticationPrincipal Funcionario funcionario) {
        String geradoPor = funcionario != null ? funcionario.getNome() : null;
        return ResponseEntity.ok(relatorioService.gerarRelatorioObra(obraId, geradoPor));
    }
}
