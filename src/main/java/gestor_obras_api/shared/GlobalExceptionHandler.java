package gestor_obras_api.shared;

import gestor_obras_api.funcionario.exception.EmailJaCadastradoException;
import gestor_obras_api.funcionario.exception.FuncionarioNotFoundException;
import gestor_obras_api.obra.exception.ObraDuplicadaException;
import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.exception.TransicaoStatusInvalidaException;
import gestor_obras_api.fornecedor.exception.FornecedorNotFoundException;
import gestor_obras_api.fornecedor.exception.FornecedorEmailJaCadastradoException;
import gestor_obras_api.custo.exception.CustoNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObraNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleObraNotFound(
            ObraNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(TransicaoStatusInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleTransicaoInvalida(
            TransicaoStatusInvalidaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ObraDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleObraDuplicada(
            ObraDuplicadaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(FuncionarioNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFuncionarioNotFound(
            FuncionarioNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<Map<String, Object>> handleEmailJaCadastrado(
            EmailJaCadastradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(FornecedorNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFornecedorNotFound(
            FornecedorNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(FornecedorEmailJaCadastradoException.class)
    public ResponseEntity<Map<String, Object>> handleFornecedorEmailJaCadastrado(
            FornecedorEmailJaCadastradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CustoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustoNotFound(
            CustoNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> campos = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            campos.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> body = buildErrorBody(HttpStatus.BAD_REQUEST, "Erro de validação", request.getRequestURI());
        body.put("campos", campos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(buildErrorBody(status, message, path));
    }

    private Map<String, Object> buildErrorBody(HttpStatus status, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}
