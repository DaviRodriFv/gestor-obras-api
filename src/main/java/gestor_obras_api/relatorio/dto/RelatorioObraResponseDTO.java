package gestor_obras_api.relatorio.dto;

import gestor_obras_api.cronograma.dto.CronogramaResponseDTO;
import gestor_obras_api.custo.dto.CustoResponseDTO;
import gestor_obras_api.financeiro.dto.DashboardFinanceiroResponseDTO;
import gestor_obras_api.obra.dto.ObraResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Relatório de progresso e financeiro de uma obra (caso de uso Emitir Relatórios),
 * pensado para ser apresentado a clientes e investidores.
 */
public record RelatorioObraResponseDTO(
    LocalDateTime geradoEm,
    String geradoPor,
    ObraResponseDTO obra,
    CronogramaResponseDTO cronograma,
    DashboardFinanceiroResponseDTO financeiro,
    List<CustoResponseDTO> custos,
    ResumoRelatorioDTO resumo
) {

    /** Indicadores de destaque do relatório, calculados a partir do cronograma e dos custos. */
    public record ResumoRelatorioDTO(
        int totalEtapas,
        int etapasConcluidas,
        int etapasAtrasadas,
        BigDecimal totalPago,
        BigDecimal totalPendente
    ) {
    }
}
