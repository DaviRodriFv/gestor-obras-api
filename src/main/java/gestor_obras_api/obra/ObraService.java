package gestor_obras_api.obra;

import gestor_obras_api.dto.AlterarStatusDTO;
import gestor_obras_api.dto.ObraRequestDTO;
import gestor_obras_api.dto.ObraResponseDTO;
import gestor_obras_api.model.Funcionario;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.exception.TransicaoStatusInvalidaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ObraService {

    private final ObraRepository obraRepository;
    private final ObraMapper obraMapper;


    private static final Map<StatusObra, Set<StatusObra>> TRANSICOES_PERMITIDAS;

    static {
        TRANSICOES_PERMITIDAS = new EnumMap<>(StatusObra.class);
        TRANSICOES_PERMITIDAS.put(StatusObra.EM_ANDAMENTO,
            EnumSet.of(StatusObra.PAUSADA, StatusObra.CONCLUIDA, StatusObra.CANCELADA));
        TRANSICOES_PERMITIDAS.put(StatusObra.PAUSADA,
            EnumSet.of(StatusObra.EM_ANDAMENTO, StatusObra.CONCLUIDA, StatusObra.CANCELADA));
    }

    @Transactional(readOnly = true)
    public List<ObraResponseDTO> listar(StatusObra status, String busca) {
        boolean temBusca = busca != null && !busca.isBlank();
        List<Obra> obras;
        if (status != null && temBusca) {
            obras = obraRepository.findByStatusAndNomeOrClienteContaining(status, busca);
        } else if (status != null) {
            obras = obraRepository.findByStatus(status);
        } else if (temBusca) {
            obras = obraRepository.findByNomeOrClienteContainingIgnoreCase(busca);
        } else {
            obras = obraRepository.findAll();
        }
        return obras.stream().map(obraMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ObraResponseDTO buscarPorId(UUID id) {
        return obraMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public ObraResponseDTO criar(ObraRequestDTO dto, Funcionario funcionario) {
        validarDatas(dto);
        validarStatusCriacao(dto.getStatus());
        Obra obra = obraMapper.toEntity(dto, funcionario);
        return obraMapper.toResponse(obraRepository.save(obra));
    }

    @Transactional
    public ObraResponseDTO atualizar(UUID id, ObraRequestDTO dto, Funcionario funcionario) {
        validarDatas(dto);
        Obra obra = findOrThrow(id);
        if (obra.getStatus().isTerminal()) {
            throw new TransicaoStatusInvalidaException(
                "Obra em estado terminal (" + obra.getStatus() + ") não pode ser editada");
        }
        obraMapper.updateEntity(obra, dto, funcionario);
        return obraMapper.toResponse(obraRepository.save(obra));
    }

    @Transactional
    public ObraResponseDTO alterarStatus(UUID id, AlterarStatusDTO dto) {
        Obra obra = findOrThrow(id);
        StatusObra atual = obra.getStatus();
        StatusObra novo = dto.getNovoStatus();

        validarTransicao(atual, novo);

        if (novo == StatusObra.CONCLUIDA && !podeConcluir(obra)) {
            throw new TransicaoStatusInvalidaException(atual, novo);
        }

        obra.setStatus(novo);
        onEntrarEstado(obra, novo);
        return obraMapper.toResponse(obraRepository.save(obra));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!obraRepository.existsById(id)) {
            throw new ObraNotFoundException(id);
        }
        obraRepository.deleteById(id);
    }

    // ---- helpers privados ----

    private Obra findOrThrow(UUID id) {
        return obraRepository.findById(id)
            .orElseThrow(() -> new ObraNotFoundException(id));
    }

    private void validarDatas(ObraRequestDTO dto) {
        if (dto.getPrazoConclusao() != null && dto.getDataInicio() != null
                && dto.getPrazoConclusao().isBefore(dto.getDataInicio())) {
            throw new IllegalArgumentException("prazoConclusao deve ser >= dataInicio");
        }
    }

    private void validarStatusCriacao(StatusObra status) {
        if (status == StatusObra.CANCELADA) {
            throw new IllegalArgumentException(
                "Não é permitido criar uma obra com status CANCELADA");
        }
    }

    private void validarTransicao(StatusObra de, StatusObra para) {
        Set<StatusObra> permitidos = TRANSICOES_PERMITIDAS.getOrDefault(de, Set.of());
        if (!permitidos.contains(para)) {
            throw new TransicaoStatusInvalidaException(de, para);
        }
    }

    /**
     * TODO: Integrar com módulo de Cronograma quando disponível.
     * Regra real: somatório das etapas == 100%.
     * Enquanto não integrado, qualquer obra pode ser concluída.
     */
    private boolean podeConcluir(Obra obra) {
        return true;
    }

    /**
     * Stubs para eventos de entrada em cada estado.
     * TODO: Implementar integrações com Cronograma, Financeiro e Relatórios.
     */
    private void onEntrarEstado(Obra obra, StatusObra novoStatus) {
        switch (novoStatus) {
            case EM_ANDAMENTO -> { /* TODO: iniciar/retomar cronograma */ }
            case PAUSADA      -> { /* TODO: suspender atividades; manter registros financeiros */ }
            case CONCLUIDA    -> { /* TODO: fechar controle financeiro; gerar relatório; arquivar */ }
            case CANCELADA    -> { /* TODO: registrar motivo; arquivar registros */ }
        }
    }
}
