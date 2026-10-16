package gestor_obras_api.custo.service;

import gestor_obras_api.custo.dto.AlterarStatusCustoDTO;
import gestor_obras_api.custo.dto.AnexoCustoDTO;
import gestor_obras_api.custo.dto.CustoRequestDTO;
import gestor_obras_api.custo.dto.CustoResponseDTO;
import gestor_obras_api.custo.dto.CustoUpdateDTO;
import gestor_obras_api.custo.dto.GastoCategoriaDTO;
import gestor_obras_api.custo.exception.AnexoNaoEncontradoException;
import gestor_obras_api.custo.exception.CustoNotFoundException;
import gestor_obras_api.custo.model.CategoriaCusto;
import gestor_obras_api.custo.model.Custo;
import gestor_obras_api.custo.model.CustoAnexo;
import gestor_obras_api.custo.model.StatusCusto;
import gestor_obras_api.custo.repository.CustoAnexoRepository;
import gestor_obras_api.custo.repository.CustoRepository;
import gestor_obras_api.custo.repository.TotalPorCategoria;
import gestor_obras_api.fornecedor.exception.FornecedorNotFoundException;
import gestor_obras_api.fornecedor.model.Fornecedor;
import gestor_obras_api.fornecedor.repository.FornecedorRepository;
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
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustoServiceTest {

    @Mock private CustoRepository custoRepository;
    @Mock private CustoAnexoRepository custoAnexoRepository;
    @Mock private ObraRepository obraRepository;
    @Mock private FornecedorRepository fornecedorRepository;
    @InjectMocks private CustoService custoService;

    private UUID obraId;
    private Obra obra;
    private CustoRequestDTO dto;

    @BeforeEach
    void setUp() {
        obraId = UUID.randomUUID();
        obra = new Obra();
        obra.setId(obraId);

        dto = new CustoRequestDTO();
        dto.setCategoria(CategoriaCusto.MATERIAL);
        dto.setValor(new BigDecimal("1500.00"));
        dto.setData(LocalDate.of(2026, 10, 1));
        dto.setDescricao("Cimento");
    }

    @Test
    void criar_semStatus_deveSalvarComoPendente() {
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obra));
        when(custoRepository.save(any(Custo.class))).thenAnswer(inv -> inv.getArgument(0));

        CustoResponseDTO resultado = custoService.criar(obraId, dto);

        assertThat(resultado.getStatus()).isEqualTo(StatusCusto.PENDENTE);
        assertThat(resultado.getObraId()).isEqualTo(obraId);
        assertThat(resultado.getFornecedorId()).isNull();
    }

    @Test
    void criar_comFornecedorEStatusPago_deveVincularFornecedor() {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setId(UUID.randomUUID());
        fornecedor.setNome("Depósito Silva");
        dto.setFornecedorId(fornecedor.getId());
        dto.setStatus(StatusCusto.PAGO);
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obra));
        when(fornecedorRepository.findById(fornecedor.getId())).thenReturn(Optional.of(fornecedor));
        when(custoRepository.save(any(Custo.class))).thenAnswer(inv -> inv.getArgument(0));

        CustoResponseDTO resultado = custoService.criar(obraId, dto);

        assertThat(resultado.getFornecedorNome()).isEqualTo("Depósito Silva");
        assertThat(resultado.getStatus()).isEqualTo(StatusCusto.PAGO);
    }

    @Test
    void criar_comObraInexistente_deveLancarObraNotFound() {
        when(obraRepository.findById(obraId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> custoService.criar(obraId, dto))
            .isInstanceOf(ObraNotFoundException.class);
        verify(custoRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({"0", "0.00", "-10"})
    void criar_comValorMenorOuIgualAZero_deveLancarIllegalArgument(String valor) {
        dto.setValor(new BigDecimal(valor));
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obra));

        assertThatThrownBy(() -> custoService.criar(obraId, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Dados do lançamento inválidos");
        verify(custoRepository, never()).save(any());
    }

    @Test
    void criar_comStatusCancelado_deveLancarIllegalArgument() {
        dto.setStatus(StatusCusto.CANCELADO);
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obra));

        assertThatThrownBy(() -> custoService.criar(obraId, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Dados do lançamento inválidos");
        verify(custoRepository, never()).save(any());
    }

    @Test
    void criar_comFornecedorInexistente_deveLancarFornecedorNotFound() {
        UUID fornecedorId = UUID.randomUUID();
        dto.setFornecedorId(fornecedorId);
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obra));
        when(fornecedorRepository.findById(fornecedorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> custoService.criar(obraId, dto))
            .isInstanceOf(FornecedorNotFoundException.class);
        verify(custoRepository, never()).save(any());
    }

    @Test
    void atualizar_naoAlteraStatus() {
        Custo custo = custo(StatusCusto.PAGO);
        mockCustoDaObra(custo);
        when(custoRepository.save(custo)).thenReturn(custo);

        CustoUpdateDTO update = new CustoUpdateDTO();
        update.setCategoria(CategoriaCusto.MAO_DE_OBRA);
        update.setValor(new BigDecimal("900.00"));
        update.setData(LocalDate.of(2026, 10, 2));
        update.setDescricao("Pedreiro");

        CustoResponseDTO resultado = custoService.atualizar(obraId, custo.getId(), update);

        assertThat(resultado.getValor()).isEqualByComparingTo("900.00");
        assertThat(resultado.getCategoria()).isEqualTo(CategoriaCusto.MAO_DE_OBRA);
        assertThat(resultado.getStatus()).isEqualTo(StatusCusto.PAGO);
    }

    @Test
    void atualizar_comValorZero_deveLancarIllegalArgument() {
        Custo custo = custo(StatusCusto.PENDENTE);
        mockCustoDaObra(custo);

        CustoUpdateDTO update = new CustoUpdateDTO();
        update.setValor(BigDecimal.ZERO);

        assertThatThrownBy(() -> custoService.atualizar(obraId, custo.getId(), update))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Dados do lançamento inválidos");
    }

    @Test
    void buscarPorId_deCustoDeOutraObra_deveLancarCustoNotFound() {
        UUID custoId = UUID.randomUUID();
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obra));
        when(custoRepository.findByIdAndObraId(custoId, obraId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> custoService.buscarPorId(obraId, custoId))
            .isInstanceOf(CustoNotFoundException.class);
    }

    @ParameterizedTest
    @CsvSource({"PENDENTE, PAGO", "PENDENTE, CANCELADO"})
    void alterarStatus_transicoesPermitidas(StatusCusto de, StatusCusto para) {
        Custo custo = custo(de);
        mockCustoDaObra(custo);
        when(custoRepository.save(custo)).thenReturn(custo);

        CustoResponseDTO resultado = custoService.alterarStatus(obraId, custo.getId(), statusDTO(para));

        assertThat(resultado.getStatus()).isEqualTo(para);
    }

    @ParameterizedTest
    @CsvSource({
        "PENDENTE, PENDENTE",
        "PAGO, PENDENTE", "PAGO, CANCELADO", "PAGO, PAGO",
        "CANCELADO, PENDENTE", "CANCELADO, PAGO", "CANCELADO, CANCELADO"
    })
    void alterarStatus_transicoesInvalidas_deveLancarIllegalArgument(StatusCusto de, StatusCusto para) {
        Custo custo = custo(de);
        mockCustoDaObra(custo);

        assertThatThrownBy(() -> custoService.alterarStatus(obraId, custo.getId(), statusDTO(para)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageStartingWith("Transição de status inválida");
        verify(custoRepository, never()).save(any());
    }

    @Test
    void somarPorCategoria_retornaAsTresCategoriasComZeroQuandoNaoHaLancamento() {
        obra.setOrcamento(new BigDecimal("10000.00"));
        // A query já exclui os CANCELADO; aqui só MATERIAL tem lançamentos válidos
        when(custoRepository.somarPorCategoria(obraId)).thenReturn(List.of(
            new TotalPorCategoria(CategoriaCusto.MATERIAL, new BigDecimal("2500.00"), 2L)));

        List<GastoCategoriaDTO> resultado = custoService.somarPorCategoria(obra);

        assertThat(resultado).extracting(GastoCategoriaDTO::getCategoria)
            .containsExactly(CategoriaCusto.values());
        GastoCategoriaDTO material = resultado.get(0);
        assertThat(material.getTotal()).isEqualByComparingTo("2500.00");
        assertThat(material.getQuantidade()).isEqualTo(2);
        assertThat(material.getPercentualOrcamento()).isEqualByComparingTo("25.00");
        assertThat(resultado.get(1).getTotal()).isEqualByComparingTo("0");
        assertThat(resultado.get(1).getQuantidade()).isZero();
        assertThat(resultado.get(1).getPercentualOrcamento()).isEqualByComparingTo("0");
    }

    @Test
    void somarPorCategoria_semOrcamento_percentualNulo() {
        when(custoRepository.somarPorCategoria(obraId)).thenReturn(List.of(
            new TotalPorCategoria(CategoriaCusto.MATERIAL, new BigDecimal("100.00"), 1L)));

        List<GastoCategoriaDTO> resultado = custoService.somarPorCategoria(obra);

        assertThat(resultado).allSatisfy(g -> assertThat(g.getPercentualOrcamento()).isNull());
    }

    @Test
    void calcularPercentual_arredondaHalfUp() {
        assertThat(CustoService.calcularPercentual(new BigDecimal("1"), new BigDecimal("3")))
            .isEqualByComparingTo("33.33");
        assertThat(CustoService.calcularPercentual(new BigDecimal("2"), new BigDecimal("3")))
            .isEqualByComparingTo("66.67");
    }

    @Test
    void anexarArquivo_pdf_deveSalvarConteudoENomeNoCusto() {
        Custo custo = custo(StatusCusto.PENDENTE);
        mockCustoDaObra(custo);
        when(custoAnexoRepository.findByCustoId(custo.getId())).thenReturn(Optional.empty());
        when(custoRepository.save(custo)).thenReturn(custo);
        MockMultipartFile pdf = new MockMultipartFile("arquivo", "orcamento-piso.pdf", "application/pdf", new byte[]{1, 2, 3});

        CustoResponseDTO resultado = custoService.anexarArquivo(obraId, custo.getId(), pdf);

        assertThat(resultado.isPossuiAnexo()).isTrue();
        assertThat(resultado.getAnexoNome()).isEqualTo("orcamento-piso.pdf");
        verify(custoAnexoRepository).save(argThat(a -> a.getCusto() == custo && a.getConteudo().length == 3));
    }

    @Test
    void anexarArquivo_quandoJaTemAnexo_deveSubstituir() {
        Custo custo = custo(StatusCusto.PAGO);
        mockCustoDaObra(custo);
        CustoAnexo existente = new CustoAnexo();
        existente.setCusto(custo);
        existente.setConteudo(new byte[]{9});
        when(custoAnexoRepository.findByCustoId(custo.getId())).thenReturn(Optional.of(existente));
        when(custoRepository.save(custo)).thenReturn(custo);
        MockMultipartFile foto = new MockMultipartFile("arquivo", "nota.jpg", "image/jpeg", new byte[]{4, 5});

        custoService.anexarArquivo(obraId, custo.getId(), foto);

        assertThat(existente.getConteudo()).containsExactly(4, 5);
        assertThat(custo.getAnexoNome()).isEqualTo("nota.jpg");
        assertThat(custo.getAnexoTipo()).isEqualTo("image/jpeg");
    }

    @Test
    void anexarArquivo_tipoNaoPermitido_deveLancarIllegalArgument() {
        Custo custo = custo(StatusCusto.PENDENTE);
        mockCustoDaObra(custo);
        MockMultipartFile planilha = new MockMultipartFile("arquivo", "custos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{1});

        assertThatThrownBy(() -> custoService.anexarArquivo(obraId, custo.getId(), planilha))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("O anexo deve ser um PDF ou uma imagem (PNG ou JPG)");
        verify(custoAnexoRepository, never()).save(any());
    }

    @Test
    void baixarArquivo_semAnexo_deveLancarAnexoNaoEncontrado() {
        Custo custo = custo(StatusCusto.PENDENTE);
        mockCustoDaObra(custo);
        when(custoAnexoRepository.findByCustoId(custo.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> custoService.baixarArquivo(obraId, custo.getId()))
            .isInstanceOf(AnexoNaoEncontradoException.class);
    }

    @Test
    void baixarArquivo_comAnexo_deveRetornarNomeTipoEConteudo() {
        Custo custo = custo(StatusCusto.PENDENTE);
        custo.setAnexoNome("orcamento.pdf");
        custo.setAnexoTipo("application/pdf");
        mockCustoDaObra(custo);
        CustoAnexo anexo = new CustoAnexo();
        anexo.setConteudo(new byte[]{7, 8});
        when(custoAnexoRepository.findByCustoId(custo.getId())).thenReturn(Optional.of(anexo));

        AnexoCustoDTO resultado = custoService.baixarArquivo(obraId, custo.getId());

        assertThat(resultado.nome()).isEqualTo("orcamento.pdf");
        assertThat(resultado.tipo()).isEqualTo("application/pdf");
        assertThat(resultado.conteudo()).containsExactly(7, 8);
    }

    @Test
    void removerArquivo_deveApagarAnexoELimparNome() {
        Custo custo = custo(StatusCusto.PENDENTE);
        custo.setAnexoNome("orcamento.pdf");
        custo.setAnexoTipo("application/pdf");
        mockCustoDaObra(custo);
        CustoAnexo anexo = new CustoAnexo();
        when(custoAnexoRepository.findByCustoId(custo.getId())).thenReturn(Optional.of(anexo));
        when(custoRepository.save(custo)).thenReturn(custo);

        CustoResponseDTO resultado = custoService.removerArquivo(obraId, custo.getId());

        verify(custoAnexoRepository).delete(anexo);
        assertThat(resultado.isPossuiAnexo()).isFalse();
        assertThat(resultado.getAnexoNome()).isNull();
    }

    private void mockCustoDaObra(Custo custo) {
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obra));
        when(custoRepository.findByIdAndObraId(custo.getId(), obraId)).thenReturn(Optional.of(custo));
    }

    private Custo custo(StatusCusto status) {
        Custo c = new Custo();
        c.setId(UUID.randomUUID());
        c.setObra(obra);
        c.setCategoria(CategoriaCusto.MATERIAL);
        c.setValor(new BigDecimal("100.00"));
        c.setData(LocalDate.of(2026, 10, 1));
        c.setDescricao("Areia");
        c.setStatus(status);
        return c;
    }

    private AlterarStatusCustoDTO statusDTO(StatusCusto status) {
        AlterarStatusCustoDTO d = new AlterarStatusCustoDTO();
        d.setStatus(status);
        return d;
    }
}
