package gestor_obras_api.relatorio.service;

import gestor_obras_api.cronograma.dto.CronogramaResponseDTO;
import gestor_obras_api.cronograma.dto.EtapaResponseDTO;
import gestor_obras_api.cronograma.model.StatusEtapa;
import gestor_obras_api.cronograma.service.EtapaService;
import gestor_obras_api.custo.dto.CustoResponseDTO;
import gestor_obras_api.custo.model.StatusCusto;
import gestor_obras_api.custo.service.CustoService;
import gestor_obras_api.financeiro.dto.DashboardFinanceiroResponseDTO;
import gestor_obras_api.financeiro.service.DashboardFinanceiroService;
import gestor_obras_api.obra.dto.ObraResponseDTO;
import gestor_obras_api.obra.service.ObraService;
import gestor_obras_api.relatorio.dto.RelatorioObraResponseDTO;
import gestor_obras_api.relatorio.dto.RelatorioObraResponseDTO.ResumoRelatorioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Emitir Relatórios: reúne em um só documento os dados da obra, o progresso do cronograma
 * e o resumo financeiro. Não persiste nada — só consulta os outros módulos.
 */
@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final ObraService obraService;
    private final EtapaService etapaService;
    private final DashboardFinanceiroService dashboardFinanceiroService;
    private final CustoService custoService;

    @Transactional(readOnly = true)
    public RelatorioObraResponseDTO gerarRelatorioObra(UUID obraId, String geradoPor) {
        // buscarPorId lança ObraNotFoundException (404) antes de consultar os demais módulos
        ObraResponseDTO obra = obraService.buscarPorId(obraId);
        CronogramaResponseDTO cronograma = etapaService.buscarCronograma(obraId);
        DashboardFinanceiroResponseDTO financeiro = dashboardFinanceiroService.gerarResumo(obraId);
        List<CustoResponseDTO> custos = custoService.listar(obraId, null, null, null);

        return new RelatorioObraResponseDTO(
            LocalDateTime.now(),
            geradoPor,
            obra,
            cronograma,
            financeiro,
            custos,
            calcularResumo(cronograma.getEtapas(), custos)
        );
    }

    ResumoRelatorioDTO calcularResumo(List<EtapaResponseDTO> etapas, List<CustoResponseDTO> custos) {
        int concluidas = (int) etapas.stream().filter(e -> e.getStatus() == StatusEtapa.CONCLUIDA).count();
        int atrasadas = (int) etapas.stream().filter(e -> e.getStatus() == StatusEtapa.ATRASADA).count();
        return new ResumoRelatorioDTO(
            etapas.size(),
            concluidas,
            atrasadas,
            somarPorStatus(custos, StatusCusto.PAGO),
            somarPorStatus(custos, StatusCusto.PENDENTE)
        );
    }

    private BigDecimal somarPorStatus(List<CustoResponseDTO> custos, StatusCusto status) {
        return custos.stream()
            .filter(c -> c.getStatus() == status)
            .map(CustoResponseDTO::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
