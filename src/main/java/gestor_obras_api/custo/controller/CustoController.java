package gestor_obras_api.custo.controller;

import gestor_obras_api.custo.dto.CustoRequestDTO;
import gestor_obras_api.custo.dto.CustoResponseDTO;
import gestor_obras_api.custo.dto.CustoUpdateDTO;
import gestor_obras_api.custo.service.CustoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/custos")
@RequiredArgsConstructor
public class CustoController {

    private final CustoService custoService;

    @GetMapping
    public ResponseEntity<List<CustoResponseDTO>> listarTodos(
            @RequestParam(required = false) UUID obraId) {
        return ResponseEntity.ok(custoService.listarTodos(obraId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustoResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(custoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CustoResponseDTO> criar(@Valid @RequestBody CustoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(custoService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustoResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody CustoUpdateDTO dto
    ) {
        return ResponseEntity.ok(custoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        custoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
