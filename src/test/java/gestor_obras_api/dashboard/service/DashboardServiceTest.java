package gestor_obras_api.dashboard.service;

import gestor_obras_api.cronograma.model.Etapa;
import gestor_obras_api.cronograma.model.StatusEtapa;
import gestor_obras_api.cronograma.repository.EtapaRepository;
import gestor_obras_api.dashboard.dto.DashboardResponseDTO;
import gestor_obras_api.dashboard.dto.DashboardResponseDTO.ObraResumoDTO;
import gestor_obras_api.financeiro.dto.DashboardFinanceiroResponseDTO;
import gestor_obras_api.financeiro.model.SituacaoFinanceira;
import gestor_obras_api.financeiro.service.DashboardFinanceiroService;
import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.model.StatusObra;
import gestor_obras_api.obra.repository.ObraRepository;
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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 16);

    @Mock private ObraRepository obraRepository;
    @Mock private EtapaRepository etapaRepository;
    @Mock private DashboardFinanceiroService dashboardFinanceiroService;
    @InjectMocks private DashboardService dashboardService;

    @Test
    void gerarDashboard_calculaProgressoProximaEtapaEAlertas() {
        Obra obra = obra("Residencial Alpha", StatusObra.EM_ANDAMENTO, HOJE.plusDays(10));
        Etapa fundacao = etapa(obra, "Fundação", StatusEtapa.CONCLUIDA, 100, HOJE.minusDays(60));
        Etapa estrutura = etapa(obra, "Estrutura", StatusEtapa.ATRASADA, 40, HOJE.minusDays(20));
        Etapa alvenaria = etapa(obra, "Alvenaria", StatusEtapa.NAO_INICIADA, 0, HOJE.plusDays(5));
        when(obraRepository.findAll()).thenReturn(List.of(obra));
        when(etapaRepository.findByObraIdIn(anyCollection())).thenReturn(List.of(fundacao, estrutura, alvenaria));

        DashboardResponseDTO dashboard = dashboardService.gerarDashboard(false, HOJE);

        ObraResumoDTO resumo = dashboard.obras().get(0);
        assertThat(resumo.progressoGeral()).isEqualTo(46); // (100 + 40 + 0) / 3
        assertThat(resumo.totalEtapas()).isEqualTo(3);
        assertThat(resumo.etapasAtrasadas()).isEqualTo(1);
        // Próximo passo: a etapa não concluída com a data prevista mais antiga
        assertThat(resumo.proximaEtapa()).isEqualTo("Estrutura");
        assertThat(resumo.diasParaPrazo()).isEqualTo(10);
        assertThat(dashboard.obrasComPrazoProximo()).isEqualTo(1);
        assertThat(dashboard.obrasComPrazoVencido()).isZero();
        assertThat(dashboard.obrasComEtapasAtrasadas()).isEqualTo(1);
    }

    @Test
    void gerarDashboard_contaStatusEOrdenaAtivasPrimeiroPeloPrazo() {
        Obra concluida = obra("Concluída", StatusObra.CONCLUIDA, HOJE.minusDays(100));
        Obra vencida = obra("Vencida", StatusObra.EM_ANDAMENTO, HOJE.minusDays(3));
        Obra pausada = obra("Pausada", StatusObra.PAUSADA, HOJE.plusDays(90));
        when(obraRepository.findAll()).thenReturn(List.of(concluida, pausada, vencida));
        when(etapaRepository.findByObraIdIn(anyCollection())).thenReturn(List.of());

        DashboardResponseDTO dashboard = dashboardService.gerarDashboard(false, HOJE);

        assertThat(dashboard.obras()).extracting(ObraResumoDTO::nome)
            .containsExactly("Vencida", "Pausada", "Concluída");
        assertThat(dashboard.totalObras()).isEqualTo(3);
        assertThat(dashboard.obrasEmAndamento()).isEqualTo(1);
        assertThat(dashboard.obrasPausadas()).isEqualTo(1);
        assertThat(dashboard.obrasConcluidas()).isEqualTo(1);
        assertThat(dashboard.obrasComPrazoVencido()).isEqualTo(1);
        // Obra concluída não tem alerta de prazo, mesmo com o prazo no passado
        assertThat(dashboard.obras().get(2).diasParaPrazo()).isNull();
        assertThat(dashboard.obras().get(0).proximaEtapa()).isNull();
        assertThat(dashboard.obras().get(0).progressoGeral()).isZero();
    }

    @Test
    void gerarDashboard_semFinanceiro_naoConsultaDadosFinanceiros() {
        when(obraRepository.findAll()).thenReturn(List.of(obra("Obra", StatusObra.EM_ANDAMENTO, HOJE.plusDays(200))));
        when(etapaRepository.findByObraIdIn(anyCollection())).thenReturn(List.of());

        DashboardResponseDTO dashboard = dashboardService.gerarDashboard(false, HOJE);

        assertThat(dashboard.obrasComOrcamentoEmRisco()).isNull();
        assertThat(dashboard.obras().get(0).situacaoFinanceira()).isNull();
        verifyNoInteractions(dashboardFinanceiroService);
    }

    @Test
    void gerarDashboard_comFinanceiro_contaObrasComOrcamentoEmRisco() {
        Obra alerta = obra("Em alerta", StatusObra.EM_ANDAMENTO, HOJE.plusDays(200));
        Obra ok = obra("Ok", StatusObra.EM_ANDAMENTO, HOJE.plusDays(300));
        when(obraRepository.findAll()).thenReturn(List.of(alerta, ok));
        when(etapaRepository.findByObraIdIn(anyCollection())).thenReturn(List.of());
        when(dashboardFinanceiroService.gerarResumo(alerta.getId()))
            .thenReturn(financeiro(alerta, SituacaoFinanceira.EM_ALERTA, "85.00"));
        when(dashboardFinanceiroService.gerarResumo(ok.getId()))
            .thenReturn(financeiro(ok, SituacaoFinanceira.DENTRO_DO_ORCAMENTO, "20.00"));

        DashboardResponseDTO dashboard = dashboardService.gerarDashboard(true, HOJE);

        assertThat(dashboard.obrasComOrcamentoEmRisco()).isEqualTo(1);
        assertThat(dashboard.obras().get(0).situacaoFinanceira()).isEqualTo(SituacaoFinanceira.EM_ALERTA);
        assertThat(dashboard.obras().get(0).percentualOrcamentoConsumido()).isEqualByComparingTo("85.00");
    }

    @Test
    void gerarDashboard_semObras_naoConsultaEtapas() {
        when(obraRepository.findAll()).thenReturn(List.of());

        DashboardResponseDTO dashboard = dashboardService.gerarDashboard(true, HOJE);

        assertThat(dashboard.totalObras()).isZero();
        assertThat(dashboard.obras()).isEmpty();
        verify(etapaRepository, never()).findByObraIdIn(any());
    }

    private Obra obra(String nome, StatusObra status, LocalDate prazo) {
        Obra obra = new Obra();
        obra.setId(UUID.randomUUID());
        obra.setNome(nome);
        obra.setCliente("Cliente");
        obra.setStatus(status);
        obra.setDataInicio(HOJE.minusDays(120));
        obra.setPrazoConclusao(prazo);
        return obra;
    }

    private Etapa etapa(Obra obra, String nome, StatusEtapa status, int percentual, LocalDate inicioPrevisto) {
        Etapa etapa = new Etapa();
        etapa.setId(UUID.randomUUID());
        etapa.setObra(obra);
        etapa.setNome(nome);
        etapa.setStatus(status);
        etapa.setPercentualProgresso(percentual);
        etapa.setDataPrevistaInicio(inicioPrevisto);
        etapa.setDataPrevistaFim(inicioPrevisto.plusDays(30));
        return etapa;
    }

    private DashboardFinanceiroResponseDTO financeiro(Obra obra, SituacaoFinanceira situacao, String percentual) {
        return new DashboardFinanceiroResponseDTO(obra.getId(), obra.getNome(), new BigDecimal("1000.00"),
            new BigDecimal(percentual).multiply(BigDecimal.TEN), null, new BigDecimal(percentual), situacao, List.of());
    }
}
