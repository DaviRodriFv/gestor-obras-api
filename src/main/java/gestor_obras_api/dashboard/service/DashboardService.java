package gestor_obras_api.dashboard.service;

import gestor_obras_api.cronograma.model.Etapa;
import gestor_obras_api.cronograma.model.StatusEtapa;
import gestor_obras_api.cronograma.repository.EtapaRepository;
import gestor_obras_api.cronograma.service.EtapaService;
import gestor_obras_api.dashboard.dto.DashboardResponseDTO;
import gestor_obras_api.dashboard.dto.DashboardResponseDTO.ObraResumoDTO;
import gestor_obras_api.financeiro.dto.DashboardFinanceiroResponseDTO;
import gestor_obras_api.financeiro.model.SituacaoFinanceira;
import gestor_obras_api.financeiro.service.DashboardFinanceiroService;
import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.model.StatusObra;
import gestor_obras_api.obra.repository.ObraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Dashboard (tela inicial): status de todas as obras com % de conclusão, próximos passos e alertas de prazo,
 * conforme o Pedido do Investidor. Só consulta — nada é persistido.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    /** A partir de quantos dias para o prazo a obra entra em alerta de "prazo próximo". */
    static final int DIAS_ALERTA_PRAZO = 30;

    private static final Set<SituacaoFinanceira> SITUACOES_EM_RISCO =
        EnumSet.of(SituacaoFinanceira.EM_ALERTA, SituacaoFinanceira.ORCAMENTO_EXCEDIDO);

    private final ObraRepository obraRepository;
    private final EtapaRepository etapaRepository;
    private final DashboardFinanceiroService dashboardFinanceiroService;

    /** @param incluirFinanceiro só o ADMINISTRADOR vê a situação financeira das obras */
    @Transactional(readOnly = true)
    public DashboardResponseDTO gerarDashboard(boolean incluirFinanceiro) {
        return gerarDashboard(incluirFinanceiro, LocalDate.now());
    }

    DashboardResponseDTO gerarDashboard(boolean incluirFinanceiro, LocalDate hoje) {
        List<Obra> obras = obraRepository.findAll();
        Map<UUID, List<Etapa>> etapasPorObra = obras.isEmpty()
            ? Map.of()
            : etapaRepository.findByObraIdIn(obras.stream().map(Obra::getId).toList()).stream()
                .collect(Collectors.groupingBy(e -> e.getObra().getId()));

        List<ObraResumoDTO> resumos = obras.stream()
            .map(obra -> resumir(obra, etapasPorObra.getOrDefault(obra.getId(), List.of()), incluirFinanceiro, hoje))
            .sorted(Comparator
                // Obras em andamento/pausadas primeiro, depois pelo prazo mais próximo
                .comparing((ObraResumoDTO o) -> o.status().isTerminal())
                .thenComparing(ObraResumoDTO::prazoConclusao, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        return new DashboardResponseDTO(
            resumos.size(),
            contarStatus(resumos, StatusObra.EM_ANDAMENTO),
            contarStatus(resumos, StatusObra.PAUSADA),
            contarStatus(resumos, StatusObra.CONCLUIDA),
            contarStatus(resumos, StatusObra.CANCELADA),
            (int) resumos.stream().filter(o -> o.diasParaPrazo() != null && o.diasParaPrazo() < 0).count(),
            (int) resumos.stream().filter(o -> o.diasParaPrazo() != null
                && o.diasParaPrazo() >= 0 && o.diasParaPrazo() <= DIAS_ALERTA_PRAZO).count(),
            (int) resumos.stream().filter(o -> o.etapasAtrasadas() > 0).count(),
            incluirFinanceiro
                ? (int) resumos.stream().filter(o -> SITUACOES_EM_RISCO.contains(o.situacaoFinanceira())).count()
                : null,
            resumos
        );
    }

    private ObraResumoDTO resumir(Obra obra, List<Etapa> etapas, boolean incluirFinanceiro, LocalDate hoje) {
        Long diasParaPrazo = obra.getStatus().isTerminal() || obra.getPrazoConclusao() == null
            ? null
            : ChronoUnit.DAYS.between(hoje, obra.getPrazoConclusao());

        Etapa proxima = etapas.stream()
            .filter(e -> e.getStatus() != StatusEtapa.CONCLUIDA)
            .min(Comparator.comparing(Etapa::getDataPrevistaInicio, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Etapa::getNome))
            .orElse(null);

        DashboardFinanceiroResponseDTO financeiro = incluirFinanceiro
            ? dashboardFinanceiroService.gerarResumo(obra.getId())
            : null;

        return new ObraResumoDTO(
            obra.getId(),
            obra.getNome(),
            obra.getCliente(),
            obra.getStatus(),
            obra.getDataInicio(),
            obra.getPrazoConclusao(),
            diasParaPrazo,
            EtapaService.calcularProgressoGeral(etapas),
            etapas.size(),
            (int) etapas.stream().filter(e -> e.getStatus() == StatusEtapa.ATRASADA).count(),
            proxima != null ? proxima.getNome() : null,
            proxima != null ? proxima.getDataPrevistaInicio() : null,
            financeiro != null ? financeiro.getSituacao() : null,
            financeiro != null ? financeiro.getPercentualConsumido() : null
        );
    }

    private int contarStatus(List<ObraResumoDTO> obras, StatusObra status) {
        return (int) obras.stream().filter(o -> o.status() == status).count();
    }
}
