package gestor_obras_api.obra;

import gestor_obras_api.dto.AlterarStatusDTO;
import gestor_obras_api.dto.ObraRequestDTO;
import gestor_obras_api.dto.ObraResponseDTO;
import gestor_obras_api.model.Funcionario;
import gestor_obras_api.model.TipoCargo;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.exception.TransicaoStatusInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObraServiceTest {

    @Mock private ObraRepository obraRepository;
    @Mock private ObraMapper obraMapper;
    @InjectMocks private ObraService obraService;

    private UUID obraId;
    private Funcionario funcionario;
    private ObraRequestDTO dto;
    private Obra obraEmAndamento;

    @BeforeEach
    void setUp() {
        obraId = UUID.randomUUID();

        funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("Admin Teste");
        funcionario.setEmail("admin@test.com");
        funcionario.setSenha("encoded");
        funcionario.setCargo(TipoCargo.ADMINISTRADOR);
        funcionario.setTelefone("(11) 99999-9999");

        dto = new ObraRequestDTO();
        dto.setNome("Obra Teste");
        dto.setEndereco("Rua A, 123");
        dto.setCliente("Cliente Teste");
        dto.setDataInicio(LocalDate.of(2025, 1, 1));
        dto.setPrazoConclusao(LocalDate.of(2025, 12, 31));

        obraEmAndamento = new Obra();
        obraEmAndamento.setStatus(StatusObra.EM_ANDAMENTO);
        obraEmAndamento.setUsuario(funcionario);
    }

    @Test
    void criar_comDadosValidos_deveSalvarERetornarDTO() {
        when(obraMapper.toEntity(dto, funcionario)).thenReturn(obraEmAndamento);
        when(obraRepository.save(obraEmAndamento)).thenReturn(obraEmAndamento);
        when(obraMapper.toResponse(obraEmAndamento)).thenReturn(new ObraResponseDTO());

        ObraResponseDTO resultado = obraService.criar(dto, funcionario);

        assertThat(resultado).isNotNull();
        verify(obraRepository).save(obraEmAndamento);
    }

    @Test
    void criar_comStatusCancelada_deveLancarIllegalArgumentException() {
        dto.setStatus(StatusObra.CANCELADA);

        assertThatThrownBy(() -> obraService.criar(dto, funcionario))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CANCELADA");
    }

    @Test
    void buscarPorId_quandoIdNaoExiste_deveLancarObraNotFoundException() {
        UUID idInexistente = UUID.randomUUID();
        when(obraRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> obraService.buscarPorId(idInexistente))
            .isInstanceOf(ObraNotFoundException.class);
    }

    @Test
    void alterarStatus_transicaoValida_deveAtualizarStatus() {
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obraEmAndamento));
        when(obraRepository.save(obraEmAndamento)).thenReturn(obraEmAndamento);
        when(obraMapper.toResponse(obraEmAndamento)).thenReturn(new ObraResponseDTO());

        AlterarStatusDTO statusDTO = new AlterarStatusDTO();
        statusDTO.setNovoStatus(StatusObra.PAUSADA);

        obraService.alterarStatus(obraId, statusDTO);

        assertThat(obraEmAndamento.getStatus()).isEqualTo(StatusObra.PAUSADA);
    }

    @Test
    void alterarStatus_deEstadoTerminal_deveLancarTransicaoStatusInvalidaException() {
        Obra obraConcluida = new Obra();
        obraConcluida.setStatus(StatusObra.CONCLUIDA);
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obraConcluida));

        AlterarStatusDTO statusDTO = new AlterarStatusDTO();
        statusDTO.setNovoStatus(StatusObra.EM_ANDAMENTO);

        assertThatThrownBy(() -> obraService.alterarStatus(obraId, statusDTO))
            .isInstanceOf(TransicaoStatusInvalidaException.class);
    }

    @Test
    void atualizar_obraEmEstadoTerminal_deveLancarTransicaoStatusInvalidaException() {
        Obra obraCancelada = new Obra();
        obraCancelada.setStatus(StatusObra.CANCELADA);
        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obraCancelada));

        assertThatThrownBy(() -> obraService.atualizar(obraId, dto, funcionario))
            .isInstanceOf(TransicaoStatusInvalidaException.class);
    }
}
