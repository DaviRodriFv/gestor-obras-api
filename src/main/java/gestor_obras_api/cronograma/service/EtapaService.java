package gestor_obras_api.cronograma.service;

import gestor_obras_api.cronograma.dto.AtualizarProgressoDTO;
import gestor_obras_api.cronograma.dto.CronogramaResponseDTO;
import gestor_obras_api.cronograma.dto.EtapaRequestDTO;
import gestor_obras_api.cronograma.dto.EtapaResponseDTO;
import gestor_obras_api.cronograma.dto.EtapaUpdateDTO;
import gestor_obras_api.cronograma.exception.EtapaNaoPertenceObraException;
import gestor_obras_api.cronograma.exception.EtapaNotFoundException;
import gestor_obras_api.cronograma.model.Etapa;
import gestor_obras_api.cronograma.model.StatusEtapa;
import gestor_obras_api.cronograma.repository.EtapaRepository;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.model.Obra;
import gestor_obras_api.obra.repository.ObraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EtapaService {

    private final EtapaRepository etapaRepository;
    private final ObraRepository obraRepository;

    @Transactional(readOnly = true)
    public CronogramaResponseDTO buscarCronograma(UUID obraId) {
        garantirObraExiste(obraId);
        List<Etapa> etapas = etapaRepository.findByObraId(obraId);
        List<EtapaResponseDTO> etapasDTO = etapas.stream().map(this::toDTO).toList();
        return new CronogramaResponseDTO(obraId, calcularProgressoGeral(etapas), etapasDTO);
    }

    @Transactional(readOnly = true)
    public EtapaResponseDTO buscarPorId(UUID obraId, UUID etapaId) {
        return toDTO(findOrThrow(obraId, etapaId));
    }

    @Transactional
    public EtapaResponseDTO criar(UUID obraId, EtapaRequestDTO dto) {
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new ObraNotFoundException(obraId));
        validarDatas(dto.getDataPrevistaInicio(), dto.getDataPrevistaFim());

        Etapa etapa = new Etapa();
        etapa.setObra(obra);
        etapa.setNome(dto.getNome());
        etapa.setDescricao(dto.getDescricao());
        etapa.setDataPrevistaInicio(dto.getDataPrevistaInicio());
        etapa.setDataPrevistaFim(dto.getDataPrevistaFim());
        if (dto.getStatus() != null) {
            etapa.setStatus(dto.getStatus());
        }
        return toDTO(etapaRepository.save(etapa));
    }

    @Transactional
    public EtapaResponseDTO atualizar(UUID obraId, UUID etapaId, EtapaUpdateDTO dto) {
        Etapa etapa = findOrThrow(obraId, etapaId);

        LocalDate novoInicio = dto.getDataPrevistaInicio() != null ? dto.getDataPrevistaInicio() : etapa.getDataPrevistaInicio();
        LocalDate novoFim = dto.getDataPrevistaFim() != null ? dto.getDataPrevistaFim() : etapa.getDataPrevistaFim();
        validarDatas(novoInicio, novoFim);

        if (dto.getNome() != null) etapa.setNome(dto.getNome());
        if (dto.getDescricao() != null) etapa.setDescricao(dto.getDescricao());
        if (dto.getDataPrevistaInicio() != null) etapa.setDataPrevistaInicio(dto.getDataPrevistaInicio());
        if (dto.getDataPrevistaFim() != null) etapa.setDataPrevistaFim(dto.getDataPrevistaFim());

        return toDTO(etapaRepository.save(etapa));
    }

    @Transactional
    public EtapaResponseDTO atualizarProgresso(UUID obraId, UUID etapaId, AtualizarProgressoDTO dto) {
        Etapa etapa = findOrThrow(obraId, etapaId);

        if (dto.getDataRealInicio() != null) etapa.setDataRealInicio(dto.getDataRealInicio());
        if (dto.getDataRealFim() != null) etapa.setDataRealFim(dto.getDataRealFim());
        if (dto.getPercentualProgresso() != null) etapa.setPercentualProgresso(dto.getPercentualProgresso());
        if (dto.getStatus() != null) etapa.setStatus(dto.getStatus());

        boolean etapaIniciada = etapa.getStatus() != StatusEtapa.NAO_INICIADA
                || (etapa.getPercentualProgresso() != null && etapa.getPercentualProgresso() > 0);
        if (etapaIniciada && etapa.getDataRealInicio() == null) {
            throw new IllegalArgumentException(
                    "Informe a data real de início: obrigatória quando a etapa está em andamento, atrasada ou concluída");
        }

        return toDTO(etapaRepository.save(etapa));
    }

    @Transactional
    public void deletar(UUID obraId, UUID etapaId) {
        findOrThrow(obraId, etapaId);
        etapaRepository.deleteById(etapaId);
    }

    private Etapa findOrThrow(UUID obraId, UUID etapaId) {
        Etapa etapa = etapaRepository.findById(etapaId)
                .orElseThrow(() -> new EtapaNotFoundException(etapaId));
        if (!etapa.getObra().getId().equals(obraId)) {
            throw new EtapaNaoPertenceObraException(etapaId, obraId);
        }
        return etapa;
    }

    private void garantirObraExiste(UUID obraId) {
        if (!obraRepository.existsById(obraId)) {
            throw new ObraNotFoundException(obraId);
        }
    }

    private void validarDatas(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new IllegalArgumentException("dataPrevistaFim deve ser >= dataPrevistaInicio");
        }
    }

    private Integer calcularProgressoGeral(List<Etapa> etapas) {
        if (etapas.isEmpty()) {
            return 0;
        }
        int soma = etapas.stream().mapToInt(Etapa::getPercentualProgresso).sum();
        return soma / etapas.size();
    }

    private EtapaResponseDTO toDTO(Etapa e) {
        return new EtapaResponseDTO(
                e.getId(), e.getObra().getId(), e.getNome(), e.getDescricao(),
                e.getDataPrevistaInicio(), e.getDataPrevistaFim(),
                e.getDataRealInicio(), e.getDataRealFim(),
                e.getPercentualProgresso(), e.getStatus(), e.getCriadoEm()
        );
    }
}
