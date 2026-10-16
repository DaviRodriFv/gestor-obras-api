package gestor_obras_api.relatorio.service;

import gestor_obras_api.cronograma.dto.CronogramaResponseDTO;
import gestor_obras_api.cronograma.dto.EtapaResponseDTO;
import gestor_obras_api.cronograma.model.StatusEtapa;
import gestor_obras_api.cronograma.service.EtapaService;
import gestor_obras_api.custo.dto.CustoResponseDTO;
import gestor_obras_api.custo.model.CategoriaCusto;
import gestor_obras_api.custo.model.StatusCusto;
import gestor_obras_api.custo.service.CustoService;
import gestor_obras_api.financeiro.dto.DashboardFinanceiroResponseDTO;
import gestor_obras_api.financeiro.model.SituacaoFinanceira;
import gestor_obras_api.financeiro.service.DashboardFinanceiroService;
import gestor_obras_api.obra.dto.ObraResponseDTO;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.service.ObraService;
import gestor_obras_api.relatorio.dto.RelatorioObraResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock private ObraService obraService;
    @Mock private EtapaService etapaService;
    @Mock private DashboardFinanceiroService dashboardFinanceiroService;
    @Mock private CustoService custoService;
    @InjectMocks private RelatorioService relatorioService;

    private UUID obraId;

    @BeforeEach
    void setUp() {
        obraId = UUID.randomUUID();
    }

    @Test
    void gerarRelatorioObra_reuneObraCronogramaFinanceiroECustos() {
        ObraResponseDTO obra = new ObraResponseDTO();
        obra.setId(obraId);
        obra.setNome("Residencial Alpha");
        CronogramaResponseDTO cronograma = new CronogramaResponseDTO(obraId, 50, List.of(
            etapa(StatusEtapa.CONCLUIDA), etapa(StatusEtapa.ATRASADA), etapa(StatusEtapa.EM_ANDAMENTO)));
        DashboardFinanceiroResponseDTO financeiro = new DashboardFinanceiroResponseDTO(
            obraId, "Residencial Alpha", new BigDecimal("10000.00"), new BigDecimal("3000.00"),
            new BigDecimal("7000.00"), new BigDecimal("30.00"), SituacaoFinanceira.DENTRO_DO_ORCAMENTO, List.of());
        List<CustoResponseDTO> custos = List.of(
            custo(StatusCusto.PAGO, "2000.00"),
            custo(StatusCusto.PENDENTE, "1000.00"),
            custo(StatusCusto.CANCELADO, "500.00"));

        when(obraService.buscarPorId(obraId)).thenReturn(obra);
        when(etapaService.buscarCronograma(obraId)).thenReturn(cronograma);
        when(dashboardFinanceiroService.gerarResumo(obraId)).thenReturn(financeiro);
        when(custoService.listar(obraId, null, null, null)).thenReturn(custos);

        RelatorioObraResponseDTO relatorio = relatorioService.gerarRelatorioObra(obraId, "André Paulino");

        assertThat(relatorio.obra()).isSameAs(obra);
        assertThat(relatorio.cronograma()).isSameAs(cronograma);
        assertThat(relatorio.financeiro()).isSameAs(financeiro);
        assertThat(relatorio.custos()).hasSize(3);
        assertThat(relatorio.geradoPor()).isEqualTo("André Paulino");
        assertThat(relatorio.geradoEm()).isNotNull();

        RelatorioObraResponseDTO.ResumoRelatorioDTO resumo = relatorio.resumo();
        assertThat(resumo.totalEtapas()).isEqualTo(3);
        assertThat(resumo.etapasConcluidas()).isEqualTo(1);
        assertThat(resumo.etapasAtrasadas()).isEqualTo(1);
        assertThat(resumo.totalPago()).isEqualByComparingTo("2000.00");
        // Cancelados não entram em pago nem em pendente
        assertThat(resumo.totalPendente()).isEqualByComparingTo("1000.00");
    }

    @Test
    void gerarRelatorioObra_obraInexistente_naoConsultaOsDemaisModulos() {
        when(obraService.buscarPorId(obraId)).thenThrow(new ObraNotFoundException(obraId));

        assertThatThrownBy(() -> relatorioService.gerarRelatorioObra(obraId, "André Paulino"))
            .isInstanceOf(ObraNotFoundException.class);
        verifyNoInteractions(etapaService, dashboardFinanceiroService, custoService);
    }

    @Test
    void calcularResumo_semEtapasNemCustos_retornaZeros() {
        RelatorioObraResponseDTO.ResumoRelatorioDTO resumo = relatorioService.calcularResumo(List.of(), List.of());

        assertThat(resumo.totalEtapas()).isZero();
        assertThat(resumo.etapasConcluidas()).isZero();
        assertThat(resumo.etapasAtrasadas()).isZero();
        assertThat(resumo.totalPago()).isEqualByComparingTo("0");
        assertThat(resumo.totalPendente()).isEqualByComparingTo("0");
        verify(custoService, never()).listar(any(), any(), any(), any());
    }

    private EtapaResponseDTO etapa(StatusEtapa status) {
        return new EtapaResponseDTO(UUID.randomUUID(), obraId, "Etapa", null,
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1), null, null, 0, status, null);
    }

    private CustoResponseDTO custo(StatusCusto status, String valor) {
        return new CustoResponseDTO(UUID.randomUUID(), obraId, CategoriaCusto.MATERIAL, new BigDecimal(valor),
            LocalDate.of(2026, 10, 1), "Lançamento", status, null, null, false, null, null);
    }
}
