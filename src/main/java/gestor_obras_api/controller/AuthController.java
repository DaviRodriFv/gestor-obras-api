package gestor_obras_api.controller;

import gestor_obras_api.config.JwtService;
import gestor_obras_api.dto.LoginRequestDTO;
import gestor_obras_api.dto.LoginResponseDTO;
import gestor_obras_api.model.Funcionario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // JWT é stateless — o cliente descarta o token
        return ResponseEntity.noContent().build();
    }
}
