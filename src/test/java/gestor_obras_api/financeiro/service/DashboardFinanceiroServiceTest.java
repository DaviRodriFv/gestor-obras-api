package gestor_obras_api.financeiro.service;

import gestor_obras_api.custo.dto.GastoCategoriaDTO;
import gestor_obras_api.custo.model.CategoriaCusto;
import gestor_obras_api.custo.service.CustoService;
import gestor_obras_api.financeiro.dto.DashboardFinanceiroResponseDTO;
import gestor_obras_api.financeiro.dto.OrcamentoObraRequestDTO;
import gestor_obras_api.financeiro.model.SituacaoFinanceira;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.repository.ObraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardFinanceiroServiceTest {

    @Mock private ObraRepository obraRepository;
    @Mock private CustoService custoService;
    @InjectMocks private DashboardFinanceiroService service;

    private UUID obraId;
    private Obra obra;

    @BeforeEach
    void setUp() {
        obraId = UUID.randomUUID();
        obra = new Obra();
        obra.setId(obraId);
        obra.setNome("Residência Jardim Paulista");
    }

    @Test
    void gerarResumo_obraInexistente_deveLancarObraNotFound() {
        when(obraRepository.findById(obraId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.gerarResumo(obraId))
            .isInstanceOf(ObraNotFoundException.class);
    }

    @Test
    void gerarResumo_semOrcamento_situacaoSemOrcamentoESaldoEPercentualNulos() {
        mockGastos("3000.00");

        DashboardFinanceiroResponseDTO r = service.gerarResumo(obraId);

        assertThat(r.getSituacao()).isEqualTo(SituacaoFinanceira.SEM_ORCAMENTO);
        assertThat(r.getTotalGasto()).isEqualByComparingTo("3000.00");
        assertThat(r.getOrcamento()).isNull();
        assertThat(r.getSaldoRestante()).isNull();
        assertThat(r.getPercentualConsumido()).isNull();
    }

    @ParameterizedTest(name = "gasto {0} de 10000 -> {1}")
    @CsvSource({
        "0.00,     DENTRO_DO_ORCAMENTO",
        "7999.99,  DENTRO_DO_ORCAMENTO",
        "8000.00,  EM_ALERTA",
        "10000.00, EM_ALERTA",
        "10000.01, ORCAMENTO_EXCEDIDO"
    })
    void gerarResumo_situacaoPorPercentual(String gasto, SituacaoFinanceira esperada) {
        obra.setOrcamento(new BigDecimal("10000.00"));
        mockGastos(gasto);

        assertThat(service.gerarResumo(obraId).getSituacao()).isEqualTo(esperada);
    }

    @Test
    void gerarResumo_abaixoDe80_dentroDoOrcamento() {
        obra.setOrcamento(new BigDecimal("150000.00"));
        mockGastos("50000.00", "25000.00");

        DashboardFinanceiroResponseDTO r = service.gerarResumo(obraId);

        assertThat(r.getSituacao()).isEqualTo(SituacaoFinanceira.DENTRO_DO_ORCAMENTO);
        assertThat(r.getPercentualConsumido()).isEqualByComparingTo("50.00");
        assertThat(r.getSaldoRestante()).isEqualByComparingTo("75000.00");
    }

    @Test
    void gerarResumo_exatamente80_emAlerta() {
        obra.setOrcamento(new BigDecimal("150000.00"));
        mockGastos("100000.00", "20000.00");

        DashboardFinanceiroResponseDTO r = service.gerarResumo(obraId);

        assertThat(r.getSituacao()).isEqualTo(SituacaoFinanceira.EM_ALERTA);
        assertThat(r.getPercentualConsumido()).isEqualByComparingTo("80.00");
        assertThat(r.getSaldoRestante()).isEqualByComparingTo("30000.00");
    }

    @Test
    void gerarResumo_exatamente100_emAlertaComSaldoZero() {
        obra.setOrcamento(new BigDecimal("5000.00"));
        mockGastos("5000.00");

        DashboardFinanceiroResponseDTO r = service.gerarResumo(obraId);

        assertThat(r.getSituacao()).isEqualTo(SituacaoFinanceira.EM_ALERTA);
        assertThat(r.getPercentualConsumido()).isEqualByComparingTo("100.00");
        assertThat(r.getSaldoRestante()).isEqualByComparingTo("0");
    }

    @Test
    void gerarResumo_acimaDe100_excedidoComSaldoNegativo() {
        obra.setOrcamento(new BigDecimal("5000.00"));
        mockGastos("4000.00", "1500.00");

        DashboardFinanceiroResponseDTO r = service.gerarResumo(obraId);

        assertThat(r.getSituacao()).isEqualTo(SituacaoFinanceira.ORCAMENTO_EXCEDIDO);
        assertThat(r.getSaldoRestante()).isEqualByComparingTo("-500.00");
        assertThat(r.getPercentualConsumido()).isEqualByComparingTo("110.00");
    }

    @Test
    void gerarResumo_quaseOitenta_naoArredondaParaAlerta() {
        // 7999,999 / 10000 = 79,99999%: arredondado daria 80,00, mas o limite ainda não foi atingido
        obra.setOrcamento(new BigDecimal("10000.000"));
        mockGastos("7999.999");

        assertThat(service.gerarResumo(obraId).getSituacao())
            .isEqualTo(SituacaoFinanceira.DENTRO_DO_ORCAMENTO);
    }

    @Test
    void gerarResumo_totalGastoEhASomaDasCategorias() {
        // Os CANCELADO já ficam de fora em CustoRepository.somarPorCategoria
        // (coberto por CustoServiceTest e pelo teste manual); aqui o total é só a soma
        obra.setOrcamento(new BigDecimal("1000.00"));
        mockGastos("100.00", "200.00", "0");

        DashboardFinanceiroResponseDTO r = service.gerarResumo(obraId);

        assertThat(r.getTotalGasto()).isEqualByComparingTo("300.00");
        assertThat(r.getGastosPorCategoria()).hasSize(3);
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void definirOrcamento_valorMenorOuIgualAZero_deveLancarIllegalArgument(String valor) {
        OrcamentoObraRequestDTO dto = new OrcamentoObraRequestDTO();
        dto.setValor(new BigDecimal(valor));

        assertThatThrownBy(() -> service.definirOrcamento(obraId, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Orçamento deve ser maior que zero");
        verify(obraRepository, never()).save(any());
    }

    @Test
    void definirOrcamento_obraInexistente_deveLancarObraNotFound() {
        OrcamentoObraRequestDTO dto = new OrcamentoObraRequestDTO();
        dto.setValor(new BigDecimal("1000"));
        when(obraRepository.findById(obraId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.definirOrcamento(obraId, dto))
            .isInstanceOf(ObraNotFoundException.class);
    }

    @Test
    void definirOrcamento_salvaERetornaResumoRecalculado() {
        OrcamentoObraRequestDTO dto = new OrcamentoObraRequestDTO();
        dto.setValor(new BigDecimal("150000.00"));
        mockGastos("15000.00");

        DashboardFinanceiroResponseDTO r = service.definirOrcamento(obraId, dto);

        verify(obraRepository).save(obra);
        assertThat(r.getOrcamento()).isEqualByComparingTo("150000.00");
        assertThat(r.getPercentualConsumido()).isEqualByComparingTo("10.00");
        assertThat(r.getSituacao()).isEqualTo(SituacaoFinanceira.DENTRO_DO_ORCAMENTO);
    }

    /** Totais por categoria, na ordem MATERIAL, MAO_DE_OBRA, SERVICO_TERCEIRIZADO. */
    private void mockGastos(String... totais) {
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obra));
        CategoriaCusto[] categorias = CategoriaCusto.values();
        List<GastoCategoriaDTO> gastos = new ArrayList<>();
        for (int i = 0; i < categorias.length; i++) {
            BigDecimal total = i < totais.length ? new BigDecimal(totais[i]) : BigDecimal.ZERO;
            gastos.add(new GastoCategoriaDTO(categorias[i], total, total.signum() > 0 ? 1 : 0, null));
        }
        when(custoService.somarPorCategoria(obra)).thenReturn(gastos);
    }
}
