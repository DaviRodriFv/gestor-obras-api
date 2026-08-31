package gestor_obras_api.funcionario.service;

import gestor_obras_api.funcionario.dto.FuncionarioRequestDTO;
import gestor_obras_api.funcionario.dto.FuncionarioResponseDTO;
import gestor_obras_api.funcionario.dto.FuncionarioUpdateDTO;
import gestor_obras_api.funcionario.exception.EmailJaCadastradoException;
import gestor_obras_api.funcionario.exception.FuncionarioNotFoundException;
import gestor_obras_api.funcionario.model.Funcionario;
import gestor_obras_api.funcionario.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<FuncionarioResponseDTO> listarTodos() {
        return funcionarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public FuncionarioResponseDTO buscarPorId(Long id) {
        return toDTO(findOrThrow(id));
    }

    public FuncionarioResponseDTO criar(FuncionarioRequestDTO dto) {
        if (funcionarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailJaCadastradoException(dto.getEmail());
        }
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(dto.getNome());
        funcionario.setEmail(dto.getEmail());
        funcionario.setSenha(passwordEncoder.encode(dto.getSenha()));
        funcionario.setCargo(dto.getCargo());
        funcionario.setRole(dto.getRole());
        funcionario.setTelefone(dto.getTelefone());
        funcionario.setAtivo(true);
        return toDTO(funcionarioRepository.save(funcionario));
    }

    public FuncionarioResponseDTO atualizar(Long id, FuncionarioUpdateDTO dto) {
        Funcionario funcionario = findOrThrow(id);

        if (dto.getNome() != null) funcionario.setNome(dto.getNome());
        if (dto.getEmail() != null) funcionario.setEmail(dto.getEmail());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            funcionario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        if (dto.getCargo() != null) funcionario.setCargo(dto.getCargo());
        if (dto.getRole() != null) funcionario.setRole(dto.getRole());
        if (dto.getTelefone() != null) funcionario.setTelefone(dto.getTelefone());
        if (dto.getAtivo() != null) funcionario.setAtivo(dto.getAtivo());

        return toDTO(funcionarioRepository.save(funcionario));
    }

    public void deletar(Long id) {
        if (!funcionarioRepository.existsById(id)) {
            throw new FuncionarioNotFoundException(id);
        }
        funcionarioRepository.deleteById(id);
    }

    private Funcionario findOrThrow(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new FuncionarioNotFoundException(id));
    }

    private FuncionarioResponseDTO toDTO(Funcionario f) {
        return new FuncionarioResponseDTO(
                f.getId(), f.getNome(), f.getEmail(), f.getCargo(), f.getRole(), f.getTelefone(), f.getAtivo()
        );
    }
}
