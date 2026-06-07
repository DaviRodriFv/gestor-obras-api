package gestor_obras_api.obra;

import gestor_obras_api.model.Funcionario;
import gestor_obras_api.obra.dto.ObraRequestDTO;
import gestor_obras_api.obra.dto.ObraResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ObraMapper {

    public ObraResponseDTO toResponse(Obra obra) {
        return new ObraResponseDTO(
            obra.getId(),
            obra.getUsuario().getId(),
            obra.getNome(),
            obra.getEndereco(),
            obra.getCliente(),
            obra.getDataInicio(),
            obra.getPrazoConclusao(),
            obra.getStatus(),
            obra.getCriadoEm()
        );
    }

    public Obra toEntity(ObraRequestDTO dto, Funcionario funcionario) {
        Obra obra = new Obra();
        obra.setUsuario(funcionario);
        obra.setNome(dto.getNome());
        obra.setEndereco(dto.getEndereco());
        obra.setCliente(dto.getCliente());
        obra.setDataInicio(dto.getDataInicio());
        obra.setPrazoConclusao(dto.getPrazoConclusao());
        if (dto.getStatus() != null) {
            obra.setStatus(dto.getStatus());
        }
        return obra;
    }

    public void updateEntity(Obra obra, ObraRequestDTO dto, Funcionario funcionario) {
        obra.setUsuario(funcionario);
        obra.setNome(dto.getNome());
        obra.setEndereco(dto.getEndereco());
        obra.setCliente(dto.getCliente());
        obra.setDataInicio(dto.getDataInicio());
        obra.setPrazoConclusao(dto.getPrazoConclusao());
    }
}
