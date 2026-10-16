package gestor_obras_api.dashboard.controller;

import gestor_obras_api.dashboard.dto.DashboardResponseDTO;
import gestor_obras_api.dashboard.service.DashboardService;
import gestor_obras_api.funcionario.model.Funcionario;
import gestor_obras_api.funcionario.model.TipoCargo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> gerarDashboard(@AuthenticationPrincipal Funcionario funcionario) {
        // Perfil Equipe não vê dados financeiros (mesma regra de custos/financeiro no SecurityConfig)
        boolean incluirFinanceiro = funcionario != null && funcionario.getCargo() == TipoCargo.ADMINISTRADOR;
        return ResponseEntity.ok(dashboardService.gerarDashboard(incluirFinanceiro));
    }
}
