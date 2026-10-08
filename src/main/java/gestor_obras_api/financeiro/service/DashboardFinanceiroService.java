package gestor_obras_api.financeiro.service;

import gestor_obras_api.custo.dto.GastoCategoriaDTO;
import gestor_obras_api.custo.service.CustoService;
import gestor_obras_api.financeiro.dto.DashboardFinanceiroResponseDTO;
import gestor_obras_api.financeiro.dto.OrcamentoObraRequestDTO;
import gestor_obras_api.financeiro.model.SituacaoFinanceira;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.repository.ObraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardFinanceiroService {

    /** Percentual do orçamento consumido a partir do qual a obra entra EM_ALERTA. */
    static final BigDecimal LIMITE_ALERTA = BigDecimal.valueOf(80);

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final ObraRepository obraRepository;
    private final CustoService custoService;

    @Transactional(readOnly = true)
    public DashboardFinanceiroResponseDTO gerarResumo(UUID obraId) {
        Obra obra = findObraOrThrow(obraId);

        List<GastoCategoriaDTO> gastosPorCategoria = custoService.somarPorCategoria(obra);
        BigDecimal totalGasto = calcularTotalGasto(gastosPorCategoria);

        BigDecimal orcamento = obra.getOrcamento();
        Indicadores indicadores;
        if (orcamento == null) {
            indicadores = new Indicadores(totalGasto, null, null,
                SituacaoFinanceira.SEM_ORCAMENTO, gastosPorCategoria);
        } else {
            SaldoEPercentual saldoEPercentual = calcularSaldoEPercentual(orcamento, totalGasto);
            indicadores = new Indicadores(totalGasto, saldoEPercentual.saldo(), saldoEPercentual.percentual(),
                calcularSituacao(orcamento, totalGasto), gastosPorCategoria);
        }
        return toDTO(obra, indicadores);
    }

    @Transactional
    public DashboardFinanceiroResponseDTO definirOrcamento(UUID obraId, OrcamentoObraRequestDTO dto) {
        if (dto.getValor() == null || dto.getValor().signum() <= 0) {
            throw new IllegalArgumentException("Orçamento deve ser maior que zero");
        }
        Obra obra = findObraOrThrow(obraId);
        obra.setOrcamento(dto.getValor());
        obraRepository.save(obra);
        return gerarResumo(obraId);
    }

    /** Soma das categorias; os CANCELADO já vêm excluídos de somarPorCategoria. */
    BigDecimal calcularTotalGasto(List<GastoCategoriaDTO> gastosPorCategoria) {
        return gastosPorCategoria.stream()
            .map(GastoCategoriaDTO::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Saldo = orçamento − gasto (pode ser negativo); percentual com 2 casas (HALF_UP). */
    SaldoEPercentual calcularSaldoEPercentual(BigDecimal orcamento, BigDecimal totalGasto) {
        return new SaldoEPercentual(
            orcamento.subtract(totalGasto),
            CustoService.calcularPercentual(totalGasto, orcamento)
        );
    }

    /** Compara sem arredondar, para que 79,999% não seja tratado como 80%. */
    SituacaoFinanceira calcularSituacao(BigDecimal orcamento, BigDecimal totalGasto) {
        if (totalGasto.compareTo(orcamento) > 0) {
            return SituacaoFinanceira.ORCAMENTO_EXCEDIDO;
        }
        if (totalGasto.multiply(CEM).compareTo(orcamento.multiply(LIMITE_ALERTA)) >= 0) {
            return SituacaoFinanceira.EM_ALERTA;
        }
        return SituacaoFinanceira.DENTRO_DO_ORCAMENTO;
    }

    private Obra findObraOrThrow(UUID obraId) {
        return obraRepository.findById(obraId)
            .orElseThrow(() -> new ObraNotFoundException(obraId));
    }

    private DashboardFinanceiroResponseDTO toDTO(Obra obra, Indicadores i) {
        return new DashboardFinanceiroResponseDTO(
            obra.getId(), obra.getNome(), obra.getOrcamento(), i.totalGasto(),
            i.saldoRestante(), i.percentualConsumido(), i.situacao(), i.gastosPorCategoria()
        );
    }

    record SaldoEPercentual(BigDecimal saldo, BigDecimal percentual) {}

    private record Indicadores(
        BigDecimal totalGasto,
        BigDecimal saldoRestante,
        BigDecimal percentualConsumido,
        SituacaoFinanceira situacao,
        List<GastoCategoriaDTO> gastosPorCategoria
    ) {}
}
