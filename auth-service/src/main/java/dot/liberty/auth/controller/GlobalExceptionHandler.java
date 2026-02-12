package dot.liberty.auth.controller;

import dot.liberty.auth.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return generateResponseEntity(ex);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return generateResponseEntity(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<Map<String, String>> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return generateResponseEntity(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyVerified(EmailAlreadyVerifiedException ex) {
        return generateResponseEntity(ex);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidVerificationCode(InvalidVerificationCodeException ex) {
        return generateResponseEntity(ex);
    }

    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<Map<String, String>> handleVerificationCodeExpired(VerificationCodeExpiredException ex) {
        return generateResponseEntity(ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return generateResponseEntity(HttpStatus.BAD_REQUEST, errors);
    }

    private ResponseEntity<Map<String, String>> generateResponseEntity(RuntimeException ex) {
        return generateResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Map<String, String>> generateResponseEntity(
            RuntimeException ex, HttpStatus status) {

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return generateResponseEntity(status, error);
    }

    private ResponseEntity<Map<String, String>> generateResponseEntity(
            HttpStatus status, Map<String, String> error) {

        return ResponseEntity.status(status)
                .body(error);
    }

}
