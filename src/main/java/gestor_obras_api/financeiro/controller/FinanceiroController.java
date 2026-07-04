package gestor_obras_api.financeiro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/financeiro")
public class FinanceiroController {

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(Map.of("status", "ok", "mensagem", "Dashboard financeiro acessível"));
    }

    @PostMapping("/entrada")
    public ResponseEntity<Map<String, Object>> criarEntrada() {
        return ResponseEntity.ok(Map.of("status", "ok", "mensagem", "Entrada financeira criada"));
    }
}
