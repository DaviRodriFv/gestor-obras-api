package gestor_obras_api.custo.service;

import gestor_obras_api.custo.dto.CustoRequestDTO;
import gestor_obras_api.custo.dto.CustoResponseDTO;
import gestor_obras_api.custo.dto.CustoUpdateDTO;
import gestor_obras_api.custo.exception.CustoNotFoundException;
import gestor_obras_api.custo.model.Custo;
import gestor_obras_api.custo.repository.CustoRepository;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.repository.ObraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustoService {

    private final CustoRepository custoRepository;
    private final ObraRepository obraRepository;

    @Transactional(readOnly = true)
    public List<CustoResponseDTO> listarTodos(UUID obraId) {
        List<Custo> custos = obraId != null
            ? custoRepository.findByObraId(obraId)
            : custoRepository.findAll();
        return custos.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public CustoResponseDTO buscarPorId(UUID id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public CustoResponseDTO criar(CustoRequestDTO dto) {
        Obra obra = obraRepository.findById(dto.getObraId())
            .orElseThrow(() -> new ObraNotFoundException(dto.getObraId()));

        Custo custo = new Custo();
        custo.setObra(obra);
        custo.setCategoria(dto.getCategoria());
        custo.setValor(dto.getValor());
        custo.setData(dto.getData());
        custo.setDescricao(dto.getDescricao());
        return toDTO(custoRepository.save(custo));
    }

    @Transactional
    public CustoResponseDTO atualizar(UUID id, CustoUpdateDTO dto) {
        Custo custo = findOrThrow(id);

        if (dto.getCategoria() != null) custo.setCategoria(dto.getCategoria());
        if (dto.getValor() != null) custo.setValor(dto.getValor());
        if (dto.getData() != null) custo.setData(dto.getData());
        if (dto.getDescricao() != null) custo.setDescricao(dto.getDescricao());

        return toDTO(custoRepository.save(custo));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!custoRepository.existsById(id)) {
            throw new CustoNotFoundException(id);
        }
        custoRepository.deleteById(id);
    }

    private Custo findOrThrow(UUID id) {
        return custoRepository.findById(id)
                .orElseThrow(() -> new CustoNotFoundException(id));
    }

    private CustoResponseDTO toDTO(Custo c) {
        return new CustoResponseDTO(
                c.getId(), c.getObra().getId(), c.getCategoria(), c.getValor(),
                c.getData(), c.getDescricao(), c.getCriadoEm()
        );
    }
}
