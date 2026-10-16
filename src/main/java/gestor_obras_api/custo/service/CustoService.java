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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    /** Anexo aceita o PDF do orçamento/nota ou uma foto do documento. */
    private static final Set<String> TIPOS_ANEXO_PERMITIDOS = Set.of("application/pdf", "image/png", "image/jpeg");
    private static final Set<String> EXTENSOES_ANEXO_PERMITIDAS = Set.of(".pdf", ".png", ".jpg", ".jpeg");

    private final CustoRepository custoRepository;
    private final CustoAnexoRepository custoAnexoRepository;
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

    /** Anexa ou substitui o arquivo do lançamento (orçamento, nota fiscal, recibo). */
    @Transactional
    public CustoResponseDTO anexarArquivo(UUID obraId, UUID id, MultipartFile arquivo) {
        Custo custo = findOrThrow(obraId, id);
        validarAnexo(arquivo);

        CustoAnexo anexo = custoAnexoRepository.findByCustoId(custo.getId()).orElseGet(() -> {
            CustoAnexo novo = new CustoAnexo();
            novo.setCusto(custo);
            return novo;
        });
        try {
            anexo.setConteudo(arquivo.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo enviado");
        }
        custoAnexoRepository.save(anexo);

        custo.setAnexoNome(arquivo.getOriginalFilename());
        custo.setAnexoTipo(arquivo.getContentType());
        return toDTO(custoRepository.save(custo));
    }

    @Transactional(readOnly = true)
    public AnexoCustoDTO baixarArquivo(UUID obraId, UUID id) {
        Custo custo = findOrThrow(obraId, id);
        CustoAnexo anexo = custoAnexoRepository.findByCustoId(custo.getId())
            .orElseThrow(() -> new AnexoNaoEncontradoException(id));
        return new AnexoCustoDTO(custo.getAnexoNome(), custo.getAnexoTipo(), anexo.getConteudo());
    }

    @Transactional
    public CustoResponseDTO removerArquivo(UUID obraId, UUID id) {
        Custo custo = findOrThrow(obraId, id);
        custoAnexoRepository.findByCustoId(custo.getId()).ifPresent(custoAnexoRepository::delete);
        custo.setAnexoNome(null);
        custo.setAnexoTipo(null);
        return toDTO(custoRepository.save(custo));
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

    private void validarAnexo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione um arquivo para anexar");
        }
        String tipo = arquivo.getContentType();
        String nome = arquivo.getOriginalFilename();
        boolean tipoValido = tipo != null && TIPOS_ANEXO_PERMITIDOS.contains(tipo.toLowerCase(Locale.ROOT));
        boolean extensaoValida = nome != null && EXTENSOES_ANEXO_PERMITIDAS.stream()
            .anyMatch(ext -> nome.toLowerCase(Locale.ROOT).endsWith(ext));
        if (!tipoValido && !extensaoValida) {
            throw new IllegalArgumentException("O anexo deve ser um PDF ou uma imagem (PNG ou JPG)");
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
            c.possuiAnexo(),
            c.getAnexoNome(),
            c.getCriadoEm()
        );
    }
}
