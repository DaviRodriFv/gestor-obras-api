package gestor_obras_api.obra.controller;

import gestor_obras_api.funcionario.model.Funcionario;
import gestor_obras_api.obra.dto.AlterarStatusDTO;
import gestor_obras_api.obra.dto.ObraRequestDTO;
import gestor_obras_api.obra.dto.ObraResponseDTO;
import gestor_obras_api.obra.model.StatusObra;
import gestor_obras_api.obra.service.ObraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/obras")
@RequiredArgsConstructor
public class ObraController {

    private final ObraService obraService;

    @GetMapping
    public ResponseEntity<List<ObraResponseDTO>> listar(
            @RequestParam(required = false) StatusObra status,
            @RequestParam(required = false) String busca) {
        return ResponseEntity.ok(obraService.listar(status, busca));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObraResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(obraService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ObraResponseDTO> criar(
            @Valid @RequestBody ObraRequestDTO dto,
            @AuthenticationPrincipal Funcionario funcionario) {
        ObraResponseDTO criada = obraService.criar(dto, funcionario);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(criada.getId())
            .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObraResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ObraRequestDTO dto,
            @AuthenticationPrincipal Funcionario funcionario) {
        return ResponseEntity.ok(obraService.atualizar(id, dto, funcionario));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ObraResponseDTO> alterarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AlterarStatusDTO dto) {
        return ResponseEntity.ok(obraService.alterarStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        obraService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
