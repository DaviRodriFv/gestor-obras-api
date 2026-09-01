package gestor_obras_api.fornecedor.service;

import gestor_obras_api.fornecedor.dto.FornecedorRequestDTO;
import gestor_obras_api.fornecedor.dto.FornecedorResponseDTO;
import gestor_obras_api.fornecedor.dto.FornecedorUpdateDTO;
import gestor_obras_api.fornecedor.dto.ObraResumoDTO;
import gestor_obras_api.fornecedor.exception.FornecedorEmailJaCadastradoException;
import gestor_obras_api.fornecedor.exception.FornecedorNotFoundException;
import gestor_obras_api.fornecedor.model.Fornecedor;
import gestor_obras_api.fornecedor.repository.FornecedorRepository;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.repository.ObraRepository;
import gestor_obras_api.orcamento.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final ObraRepository obraRepository;
    private final OrcamentoRepository orcamentoRepository;

    @Transactional(readOnly = true)
    public List<FornecedorResponseDTO> listarTodos() {
        return fornecedorRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public FornecedorResponseDTO buscarPorId(UUID id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public FornecedorResponseDTO criar(FornecedorRequestDTO dto) {
        if (fornecedorRepository.existsByEmail(dto.getEmail())) {
            throw new FornecedorEmailJaCadastradoException(dto.getEmail());
        }
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(dto.getNome());
        fornecedor.setTipoServico(dto.getTipoServico());
        fornecedor.setTelefone(dto.getTelefone());
        fornecedor.setEmail(dto.getEmail());
        fornecedor.setEndereco(dto.getEndereco());
        fornecedor.setAtivo(true);
        return toDTO(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public FornecedorResponseDTO atualizar(UUID id, FornecedorUpdateDTO dto) {
        Fornecedor fornecedor = findOrThrow(id);

        if (dto.getNome() != null) fornecedor.setNome(dto.getNome());
        if (dto.getTipoServico() != null) fornecedor.setTipoServico(dto.getTipoServico());
        if (dto.getTelefone() != null) fornecedor.setTelefone(dto.getTelefone());
        if (dto.getEmail() != null) fornecedor.setEmail(dto.getEmail());
        if (dto.getEndereco() != null) fornecedor.setEndereco(dto.getEndereco());
        if (dto.getAtivo() != null) fornecedor.setAtivo(dto.getAtivo());

        return toDTO(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!fornecedorRepository.existsById(id)) {
            throw new FornecedorNotFoundException(id);
        }
        orcamentoRepository.deleteAll(orcamentoRepository.findByFornecedorId(id));
        fornecedorRepository.deleteById(id);
    }

    @Transactional
    public FornecedorResponseDTO vincularObra(UUID fornecedorId, UUID obraId) {
        Fornecedor fornecedor = findOrThrow(fornecedorId);
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new ObraNotFoundException(obraId));
        fornecedor.getObras().add(obra);
        return toDTO(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public FornecedorResponseDTO desvincularObra(UUID fornecedorId, UUID obraId) {
        Fornecedor fornecedor = findOrThrow(fornecedorId);
        fornecedor.getObras().removeIf(o -> o.getId().equals(obraId));
        return toDTO(fornecedorRepository.save(fornecedor));
    }

    private Fornecedor findOrThrow(UUID id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new FornecedorNotFoundException(id));
    }

    private FornecedorResponseDTO toDTO(Fornecedor f) {
        List<ObraResumoDTO> obras = f.getObras().stream()
                .map(o -> new ObraResumoDTO(o.getId(), o.getNome()))
                .toList();
        return new FornecedorResponseDTO(
                f.getId(), f.getNome(), f.getTipoServico(), f.getTelefone(), f.getEmail(),
                f.getEndereco(), f.getCriadoEm(), f.isAtivo(), obras
        );
    }
}
