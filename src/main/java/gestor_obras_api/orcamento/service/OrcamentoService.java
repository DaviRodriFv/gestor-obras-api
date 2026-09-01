package gestor_obras_api.orcamento.service;

import gestor_obras_api.fornecedor.exception.FornecedorNotFoundException;
import gestor_obras_api.fornecedor.model.Fornecedor;
import gestor_obras_api.fornecedor.repository.FornecedorRepository;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.repository.ObraRepository;
import gestor_obras_api.orcamento.dto.ArquivoOrcamentoDTO;
import gestor_obras_api.orcamento.dto.OrcamentoItemDTO;
import gestor_obras_api.orcamento.dto.OrcamentoItemResponseDTO;
import gestor_obras_api.orcamento.dto.OrcamentoRequestDTO;
import gestor_obras_api.orcamento.dto.OrcamentoResponseDTO;
import gestor_obras_api.orcamento.dto.OrcamentoUpdateDTO;
import gestor_obras_api.orcamento.exception.ArquivoNaoEncontradoException;
import gestor_obras_api.orcamento.exception.OrcamentoNaoPertenceFornecedorException;
import gestor_obras_api.orcamento.exception.OrcamentoNotFoundException;
import gestor_obras_api.orcamento.model.Orcamento;
import gestor_obras_api.orcamento.model.OrcamentoItem;
import gestor_obras_api.orcamento.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ObraRepository obraRepository;

    @Transactional(readOnly = true)
    public List<OrcamentoResponseDTO> listarPorFornecedor(UUID fornecedorId) {
        if (!fornecedorRepository.existsById(fornecedorId)) {
            throw new FornecedorNotFoundException(fornecedorId);
        }
        return orcamentoRepository.findByFornecedorId(fornecedorId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrcamentoResponseDTO buscarPorId(UUID fornecedorId, UUID orcamentoId) {
        return toDTO(findBelongingOrThrow(fornecedorId, orcamentoId));
    }

    @Transactional
    public OrcamentoResponseDTO criar(UUID fornecedorId, OrcamentoRequestDTO dto, MultipartFile arquivo) {
        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
                .orElseThrow(() -> new FornecedorNotFoundException(fornecedorId));
        Obra obra = obraRepository.findById(dto.getObraId())
                .orElseThrow(() -> new ObraNotFoundException(dto.getObraId()));

        boolean temItens = dto.getItens() != null && !dto.getItens().isEmpty();
        boolean temArquivo = arquivo != null && !arquivo.isEmpty();
        if (!temItens && !temArquivo) {
            throw new IllegalArgumentException(
                    "Informe ao menos os itens do orçamento (material, quantidade e preço) ou anexe um arquivo PDF");
        }
        if (temArquivo) {
            validarArquivoPdf(arquivo);
        }

        Orcamento orcamento = new Orcamento();
        orcamento.setFornecedor(fornecedor);
        orcamento.setObra(obra);
        orcamento.setDescricao(dto.getDescricao());
        orcamento.setDataOrcamento(dto.getDataOrcamento());

        if (temItens) {
            dto.getItens().forEach(itemDto -> orcamento.getItens().add(toItemEntity(itemDto, orcamento)));
            orcamento.setValorTotal(somarItens(orcamento.getItens()));
        } else {
            if (dto.getValorTotal() == null) {
                throw new IllegalArgumentException(
                        "Informe o valor total do orçamento quando não houver itens detalhados");
            }
            orcamento.setValorTotal(dto.getValorTotal());
        }

        if (temArquivo) {
            aplicarArquivo(orcamento, arquivo);
        }

        return toDTO(orcamentoRepository.save(orcamento));
    }

    @Transactional
    public OrcamentoResponseDTO atualizar(
            UUID fornecedorId, UUID orcamentoId, OrcamentoUpdateDTO dto, MultipartFile arquivo) {
        Orcamento orcamento = findBelongingOrThrow(fornecedorId, orcamentoId);

        if (dto.getObraId() != null) {
            Obra obra = obraRepository.findById(dto.getObraId())
                    .orElseThrow(() -> new ObraNotFoundException(dto.getObraId()));
            orcamento.setObra(obra);
        }
        if (dto.getDescricao() != null) orcamento.setDescricao(dto.getDescricao());
        if (dto.getDataOrcamento() != null) orcamento.setDataOrcamento(dto.getDataOrcamento());

        boolean itensAlterados = dto.getItens() != null;
        if (itensAlterados) {
            orcamento.getItens().clear();
            dto.getItens().forEach(itemDto -> orcamento.getItens().add(toItemEntity(itemDto, orcamento)));
        }

        boolean temArquivoNovo = arquivo != null && !arquivo.isEmpty();
        if (temArquivoNovo) {
            validarArquivoPdf(arquivo);
            aplicarArquivo(orcamento, arquivo);
        }

        boolean temItens = !orcamento.getItens().isEmpty();
        if (temItens) {
            orcamento.setValorTotal(somarItens(orcamento.getItens()));
        } else if (dto.getValorTotal() != null) {
            orcamento.setValorTotal(dto.getValorTotal());
        } else if (itensAlterados) {
            throw new IllegalArgumentException(
                    "Informe o valor total do orçamento quando não houver itens detalhados");
        }

        if (!temItens && !orcamento.possuiArquivo()) {
            throw new IllegalArgumentException(
                    "Informe ao menos os itens do orçamento (material, quantidade e preço) ou anexe um arquivo PDF");
        }

        return toDTO(orcamentoRepository.save(orcamento));
    }

    @Transactional
    public void deletar(UUID fornecedorId, UUID orcamentoId) {
        Orcamento orcamento = findBelongingOrThrow(fornecedorId, orcamentoId);
        orcamentoRepository.delete(orcamento);
    }

    @Transactional(readOnly = true)
    public ArquivoOrcamentoDTO baixarArquivo(UUID fornecedorId, UUID orcamentoId) {
        Orcamento orcamento = findBelongingOrThrow(fornecedorId, orcamentoId);
        if (!orcamento.possuiArquivo()) {
            throw new ArquivoNaoEncontradoException(orcamentoId);
        }
        return new ArquivoOrcamentoDTO(orcamento.getArquivoNome(), orcamento.getArquivoTipo(), orcamento.getArquivoConteudo());
    }

    private Orcamento findBelongingOrThrow(UUID fornecedorId, UUID orcamentoId) {
        Orcamento orcamento = orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new OrcamentoNotFoundException(orcamentoId));
        if (!orcamento.getFornecedor().getId().equals(fornecedorId)) {
            throw new OrcamentoNaoPertenceFornecedorException(orcamentoId, fornecedorId);
        }
        return orcamento;
    }

    private void validarArquivoPdf(MultipartFile arquivo) {
        String tipo = arquivo.getContentType();
        String nome = arquivo.getOriginalFilename();
        boolean isPdf = "application/pdf".equalsIgnoreCase(tipo)
                || (nome != null && nome.toLowerCase().endsWith(".pdf"));
        if (!isPdf) {
            throw new IllegalArgumentException("O arquivo do orçamento deve ser um PDF");
        }
    }

    private void aplicarArquivo(Orcamento orcamento, MultipartFile arquivo) {
        try {
            orcamento.setArquivoNome(arquivo.getOriginalFilename());
            orcamento.setArquivoTipo(arquivo.getContentType());
            orcamento.setArquivoConteudo(arquivo.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo enviado");
        }
    }

    private OrcamentoItem toItemEntity(OrcamentoItemDTO dto, Orcamento orcamento) {
        OrcamentoItem item = new OrcamentoItem();
        item.setOrcamento(orcamento);
        item.setDescricaoMaterial(dto.getDescricaoMaterial());
        item.setQuantidade(dto.getQuantidade());
        item.setPrecoUnitario(dto.getPrecoUnitario());
        item.setSubtotal(dto.getQuantidade().multiply(dto.getPrecoUnitario()).setScale(2, RoundingMode.HALF_UP));
        return item;
    }

    private BigDecimal somarItens(List<OrcamentoItem> itens) {
        return itens.stream()
                .map(OrcamentoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrcamentoResponseDTO toDTO(Orcamento o) {
        List<OrcamentoItemResponseDTO> itens = o.getItens().stream()
                .map(i -> new OrcamentoItemResponseDTO(
                        i.getId(), i.getDescricaoMaterial(), i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal()))
                .toList();
        return new OrcamentoResponseDTO(
                o.getId(),
                o.getFornecedor().getId(),
                o.getFornecedor().getNome(),
                o.getObra().getId(),
                o.getObra().getNome(),
                o.getDescricao(),
                o.getDataOrcamento(),
                o.getValorTotal(),
                itens,
                o.getArquivoNome(),
                o.getArquivoTipo(),
                o.possuiArquivo(),
                o.getCriadoEm()
        );
    }
}
