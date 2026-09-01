package gestor_obras_api.auth.controller;

import gestor_obras_api.auth.dto.LoginRequestDTO;
import gestor_obras_api.auth.dto.LoginResponseDTO;
import gestor_obras_api.auth.dto.RedefinirSenhaRequestDTO;
import gestor_obras_api.config.JwtService;
import gestor_obras_api.funcionario.model.Funcionario;
import gestor_obras_api.funcionario.repository.FuncionarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha())
        );
        Funcionario funcionario = (Funcionario) auth.getPrincipal();
        String token = jwtService.generateToken(funcionario);
        return ResponseEntity.ok(new LoginResponseDTO(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getEmail(),
                funcionario.getCargo(),
                funcionario.getTelefone(),
                funcionario.getAtivo(),
                token
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal Funcionario funcionario) {
        return ResponseEntity.ok(Map.of(
                "id", funcionario.getId(),
                "nome", funcionario.getNome(),
                "email", funcionario.getEmail(),
                "cargo", funcionario.getCargo()
        ));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequestDTO dto) {
        Funcionario funcionario = funcionarioRepository.findByEmail(dto.getEmail())
                .orElse(null);

        if (funcionario == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Nenhuma conta encontrada com este e-mail."));
        }

        funcionario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        funcionarioRepository.save(funcionario);

        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // JWT é stateless — o cliente descarta o token
        return ResponseEntity.noContent().build();
    }
}
