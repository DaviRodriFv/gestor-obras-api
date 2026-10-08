package gestor_obras_api.custo.service;

import gestor_obras_api.custo.dto.AlterarStatusCustoDTO;
import gestor_obras_api.custo.dto.CustoRequestDTO;
import gestor_obras_api.custo.dto.CustoResponseDTO;
import gestor_obras_api.custo.dto.CustoUpdateDTO;
import gestor_obras_api.custo.dto.GastoCategoriaDTO;
import gestor_obras_api.custo.exception.CustoNotFoundException;
import gestor_obras_api.custo.model.CategoriaCusto;
import gestor_obras_api.custo.model.Custo;
import gestor_obras_api.custo.model.StatusCusto;
import gestor_obras_api.custo.repository.CustoRepository;
import gestor_obras_api.custo.repository.TotalPorCategoria;
import gestor_obras_api.fornecedor.exception.FornecedorNotFoundException;
import gestor_obras_api.fornecedor.model.Fornecedor;
import gestor_obras_api.fornecedor.repository.FornecedorRepository;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.repository.ObraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private static final Set<StatusCusto> STATUS_INICIAIS_PERMITIDOS =
        EnumSet.of(StatusCusto.PENDENTE, StatusCusto.PAGO);

    private static final Map<StatusCusto, Set<StatusCusto>> TRANSICOES_PERMITIDAS;

    static {
        TRANSICOES_PERMITIDAS = new EnumMap<>(StatusCusto.class);
        TRANSICOES_PERMITIDAS.put(StatusCusto.PENDENTE,
            EnumSet.of(StatusCusto.PAGO, StatusCusto.CANCELADO));
    }

    private final CustoRepository custoRepository;
    private final ObraRepository obraRepository;
    private final FornecedorRepository fornecedorRepository;

    @Transactional(readOnly = true)
    public List<CustoResponseDTO> listar(UUID obraId, CategoriaCusto categoria, StatusCusto status, String busca) {
        findObraOrThrow(obraId);
        String buscaPattern = (busca == null || busca.isBlank())
            ? null
            : "%" + busca.trim().toLowerCase(Locale.ROOT) + "%";
        return custoRepository.buscarPorObra(obraId, categoria, status, buscaPattern).stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public CustoResponseDTO buscarPorId(UUID obraId, UUID id) {
        return toDTO(findOrThrow(obraId, id));
    }

    @Transactional
    public CustoResponseDTO criar(UUID obraId, CustoRequestDTO dto) {
        Obra obra = findObraOrThrow(obraId);

        StatusCusto status = dto.getStatus() != null ? dto.getStatus() : StatusCusto.PENDENTE;
        validarLancamento(dto.getValor(), status);

        Fornecedor fornecedor = buscarFornecedor(dto.getFornecedorId());

        Custo custo = new Custo();
        custo.setObra(obra);
        custo.setFornecedor(fornecedor);
        custo.setCategoria(dto.getCategoria());
        custo.setValor(dto.getValor());
        custo.setData(dto.getData());
        custo.setDescricao(dto.getDescricao());
        custo.setStatus(status);
        return toDTO(custoRepository.save(custo));
    }

    @Transactional
    public CustoResponseDTO atualizar(UUID obraId, UUID id, CustoUpdateDTO dto) {
        Custo custo = findOrThrow(obraId, id);

        validarValor(dto.getValor());
        Fornecedor fornecedor = buscarFornecedor(dto.getFornecedorId());

        custo.setFornecedor(fornecedor);
        custo.setCategoria(dto.getCategoria());
        custo.setValor(dto.getValor());
        custo.setData(dto.getData());
        custo.setDescricao(dto.getDescricao());
        return toDTO(custoRepository.save(custo));
    }

    @Transactional
    public CustoResponseDTO alterarStatus(UUID obraId, UUID id, AlterarStatusCustoDTO dto) {
        Custo custo = findOrThrow(obraId, id);
        validarTransicao(custo.getStatus(), dto.getStatus());
        custo.setStatus(dto.getStatus());
        return toDTO(custoRepository.save(custo));
    }

    @Transactional
    public void deletar(UUID obraId, UUID id) {
        custoRepository.delete(findOrThrow(obraId, id));
    }

    @Transactional(readOnly = true)
    public List<GastoCategoriaDTO> listarPorCategoria(UUID obraId) {
        return somarPorCategoria(findObraOrThrow(obraId));
    }

    /**
     * Totais da obra nas 3 categorias (zeradas quando não há lançamento), sem os CANCELADO.
     * Também usado pelo DashboardFinanceiroService.
     */
    @Transactional(readOnly = true)
    public List<GastoCategoriaDTO> somarPorCategoria(Obra obra) {
        Map<CategoriaCusto, TotalPorCategoria> totais = custoRepository.somarPorCategoria(obra.getId()).stream()
            .collect(Collectors.toMap(TotalPorCategoria::categoria, Function.identity()));

        return Arrays.stream(CategoriaCusto.values())
            .map(categoria -> {
                TotalPorCategoria t = totais.get(categoria);
                BigDecimal total = t != null ? t.total() : BigDecimal.ZERO;
                long quantidade = t != null ? t.quantidade() : 0L;
                return new GastoCategoriaDTO(categoria, total, quantidade,
                    calcularPercentual(total, obra.getOrcamento()));
            })
            .toList();
    }

    /** total / orcamento × 100, com 2 casas (HALF_UP). Nulo quando não há orçamento. */
    public static BigDecimal calcularPercentual(BigDecimal total, BigDecimal orcamento) {
        if (orcamento == null || orcamento.signum() == 0) {
            return null;
        }
        return total.multiply(CEM).divide(orcamento, 2, RoundingMode.HALF_UP);
    }

    private void validarLancamento(BigDecimal valor, StatusCusto status) {
        validarValor(valor);
        if (!STATUS_INICIAIS_PERMITIDOS.contains(status)) {
            throw new IllegalArgumentException("Dados do lançamento inválidos");
        }
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("Dados do lançamento inválidos");
        }
    }

    private void validarTransicao(StatusCusto de, StatusCusto para) {
        Set<StatusCusto> permitidos = TRANSICOES_PERMITIDAS.getOrDefault(de, Set.of());
        if (!permitidos.contains(para)) {
            throw new IllegalArgumentException("Transição de status inválida: " + de + " -> " + para);
        }
    }

    private Fornecedor buscarFornecedor(UUID fornecedorId) {
        if (fornecedorId == null) {
            return null;
        }
        return fornecedorRepository.findById(fornecedorId)
            .orElseThrow(() -> new FornecedorNotFoundException(fornecedorId));
    }

    private Obra findObraOrThrow(UUID obraId) {
        return obraRepository.findById(obraId)
            .orElseThrow(() -> new ObraNotFoundException(obraId));
    }

    /** Custo de outra obra é tratado como inexistente (404). */
    private Custo findOrThrow(UUID obraId, UUID id) {
        findObraOrThrow(obraId);
        return custoRepository.findByIdAndObraId(id, obraId)
            .orElseThrow(() -> new CustoNotFoundException(id));
    }

    private CustoResponseDTO toDTO(Custo c) {
        Fornecedor f = c.getFornecedor();
        return new CustoResponseDTO(
            c.getId(), c.getObra().getId(), c.getCategoria(), c.getValor(), c.getData(),
            c.getDescricao(), c.getStatus(),
            f != null ? f.getId() : null,
            f != null ? f.getNome() : null,
            c.getCriadoEm()
        );
    }
}
