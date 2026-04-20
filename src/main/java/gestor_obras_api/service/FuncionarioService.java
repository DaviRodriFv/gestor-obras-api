package gestor_obras_api.service;

import gestor_obras_api.dto.FuncionarioRequestDTO;
import gestor_obras_api.dto.FuncionarioResponseDTO;
import gestor_obras_api.model.Funcionario;
import gestor_obras_api.repository.FuncionarioRepository;
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
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com id: " + id));
        return toDTO(funcionario);
    }

    public FuncionarioResponseDTO criar(FuncionarioRequestDTO dto) {
        if (funcionarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + dto.getEmail());
        }
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(dto.getNome());
        funcionario.setEmail(dto.getEmail());
        funcionario.setSenha(passwordEncoder.encode(dto.getSenha()));
        funcionario.setCargo(dto.getCargo());
        funcionario.setTelefone(dto.getTelefone());
        funcionario.setAtivo(true);
        return toDTO(funcionarioRepository.save(funcionario));
    }

    public FuncionarioResponseDTO atualizar(Long id, FuncionarioRequestDTO dto) {
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com id: " + id));

        if (dto.getNome() != null) funcionario.setNome(dto.getNome());
        if (dto.getEmail() != null) funcionario.setEmail(dto.getEmail());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            funcionario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        if (dto.getCargo() != null) funcionario.setCargo(dto.getCargo());
        if (dto.getTelefone() != null) funcionario.setTelefone(dto.getTelefone());
        if (dto.getAtivo() != null) funcionario.setAtivo(dto.getAtivo());

        return toDTO(funcionarioRepository.save(funcionario));
    }

    public void deletar(Long id) {
        if (!funcionarioRepository.existsById(id)) {
            throw new RuntimeException("Funcionário não encontrado com id: " + id);
        }
        funcionarioRepository.deleteById(id);
    }

    private FuncionarioResponseDTO toDTO(Funcionario f) {
        return new FuncionarioResponseDTO(
                f.getId(), f.getNome(), f.getEmail(), f.getCargo(), f.getTelefone(), f.getAtivo()
        );
    }
}
