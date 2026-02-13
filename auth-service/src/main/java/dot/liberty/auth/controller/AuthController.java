package dot.liberty.auth.controller;

import dot.liberty.auth.dto.request.*;
import dot.liberty.auth.dto.response.AuthResponse;
import dot.liberty.auth.dto.response.ValidateTokenResponse;
import dot.liberty.auth.dto.response.VerificationSentResponse;
import dot.liberty.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<VerificationSentResponse> register(@Valid @RequestBody RegisterRequest request) {
        VerificationSentResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        AuthResponse response = authService.verifyEmail(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<VerificationSentResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        VerificationSentResponse response = authService.resendVerificationCode(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateTokenResponse> validateToken(@Valid @RequestBody ValidateTokenRequest request) {
        ValidateTokenResponse response = authService.validateToken(request);

        return ResponseEntity.ok(response);
    }

}
