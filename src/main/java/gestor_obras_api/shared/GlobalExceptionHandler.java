package gestor_obras_api.shared;

import gestor_obras_api.obra.exception.ObraNotFoundException;
import gestor_obras_api.obra.exception.TransicaoStatusInvalidaException;
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
