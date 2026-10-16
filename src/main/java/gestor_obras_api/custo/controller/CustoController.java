package gestor_obras_api.custo.controller;

import gestor_obras_api.custo.dto.AlterarStatusCustoDTO;
import gestor_obras_api.custo.dto.AnexoCustoDTO;
import gestor_obras_api.custo.dto.CustoRequestDTO;
import gestor_obras_api.custo.dto.CustoResponseDTO;
import gestor_obras_api.custo.dto.CustoUpdateDTO;
import gestor_obras_api.custo.dto.GastoCategoriaDTO;
import gestor_obras_api.custo.model.CategoriaCusto;
import gestor_obras_api.custo.model.StatusCusto;
import gestor_obras_api.custo.service.CustoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/obras/{obraId}/custos")
@RequiredArgsConstructor
public class CustoController {

    private final CustoService custoService;

    @GetMapping
    public ResponseEntity<List<CustoResponseDTO>> listar(
            @PathVariable UUID obraId,
            @RequestParam(required = false) CategoriaCusto categoria,
            @RequestParam(required = false) StatusCusto status,
            @RequestParam(required = false) String busca) {
        return ResponseEntity.ok(custoService.listar(obraId, categoria, status, busca));
    }

    @GetMapping("/por-categoria")
    public ResponseEntity<List<GastoCategoriaDTO>> listarPorCategoria(@PathVariable UUID obraId) {
        return ResponseEntity.ok(custoService.listarPorCategoria(obraId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustoResponseDTO> buscarPorId(
            @PathVariable UUID obraId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(custoService.buscarPorId(obraId, id));
    }

    @PostMapping
    public ResponseEntity<CustoResponseDTO> criar(
            @PathVariable UUID obraId,
            @Valid @RequestBody CustoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(custoService.criar(obraId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustoResponseDTO> atualizar(
            @PathVariable UUID obraId,
            @PathVariable UUID id,
            @Valid @RequestBody CustoUpdateDTO dto) {
        return ResponseEntity.ok(custoService.atualizar(obraId, id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CustoResponseDTO> alterarStatus(
            @PathVariable UUID obraId,
            @PathVariable UUID id,
            @Valid @RequestBody AlterarStatusCustoDTO dto) {
        return ResponseEntity.ok(custoService.alterarStatus(obraId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID obraId,
            @PathVariable UUID id) {
        custoService.deletar(obraId, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}/anexo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustoResponseDTO> anexarArquivo(
            @PathVariable UUID obraId,
            @PathVariable UUID id,
            @RequestPart("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(custoService.anexarArquivo(obraId, id, arquivo));
    }

    @GetMapping("/{id}/anexo")
    public ResponseEntity<byte[]> baixarArquivo(
            @PathVariable UUID obraId,
            @PathVariable UUID id) {
        AnexoCustoDTO anexo = custoService.baixarArquivo(obraId, id);
        MediaType mediaType = anexo.tipo() != null
                ? MediaType.parseMediaType(anexo.tipo())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + anexo.nome() + "\"")
                .body(anexo.conteudo());
    }

    @DeleteMapping("/{id}/anexo")
    public ResponseEntity<CustoResponseDTO> removerArquivo(
            @PathVariable UUID obraId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(custoService.removerArquivo(obraId, id));
    }
}
