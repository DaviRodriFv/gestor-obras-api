package gestor_obras_api.orcamento.controller;

import gestor_obras_api.orcamento.dto.ArquivoOrcamentoDTO;
import gestor_obras_api.orcamento.dto.OrcamentoRequestDTO;
import gestor_obras_api.orcamento.dto.OrcamentoResponseDTO;
import gestor_obras_api.orcamento.dto.OrcamentoUpdateDTO;
import gestor_obras_api.orcamento.service.OrcamentoService;
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
@RequestMapping("/api/fornecedores/{fornecedorId}/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    @GetMapping
    public ResponseEntity<List<OrcamentoResponseDTO>> listar(@PathVariable UUID fornecedorId) {
        return ResponseEntity.ok(orcamentoService.listarPorFornecedor(fornecedorId));
    }

    @GetMapping("/{orcamentoId}")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorId(
            @PathVariable UUID fornecedorId, @PathVariable UUID orcamentoId) {
        return ResponseEntity.ok(orcamentoService.buscarPorId(fornecedorId, orcamentoId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrcamentoResponseDTO> criar(
            @PathVariable UUID fornecedorId,
            @Valid @RequestPart("dados") OrcamentoRequestDTO dto,
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orcamentoService.criar(fornecedorId, dto, arquivo));
    }

    @PutMapping(value = "/{orcamentoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrcamentoResponseDTO> atualizar(
            @PathVariable UUID fornecedorId,
            @PathVariable UUID orcamentoId,
            @Valid @RequestPart("dados") OrcamentoUpdateDTO dto,
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo) {
        return ResponseEntity.ok(orcamentoService.atualizar(fornecedorId, orcamentoId, dto, arquivo));
    }

    @DeleteMapping("/{orcamentoId}")
    public ResponseEntity<Void> deletar(@PathVariable UUID fornecedorId, @PathVariable UUID orcamentoId) {
        orcamentoService.deletar(fornecedorId, orcamentoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orcamentoId}/arquivo")
    public ResponseEntity<byte[]> baixarArquivo(
            @PathVariable UUID fornecedorId, @PathVariable UUID orcamentoId) {
        ArquivoOrcamentoDTO arquivo = orcamentoService.baixarArquivo(fornecedorId, orcamentoId);
        MediaType mediaType = arquivo.tipo() != null
                ? MediaType.parseMediaType(arquivo.tipo())
                : MediaType.APPLICATION_PDF;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + arquivo.nome() + "\"")
                .body(arquivo.conteudo());
    }
}
