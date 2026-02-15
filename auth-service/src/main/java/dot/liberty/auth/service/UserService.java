package dot.liberty.auth.service;

import dot.liberty.auth.dto.request.LoginRequest;
import dot.liberty.auth.dto.response.AuthResponse;
import dot.liberty.auth.dto.response.ValidateTokenResponse;
import dot.liberty.auth.dto.response.VerificationSentResponse;
import dot.liberty.auth.entity.Role;
import dot.liberty.auth.entity.User;
import dot.liberty.auth.exception.InvalidCredentialsException;
import dot.liberty.auth.repository.UserRepository;
import dot.liberty.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class UserService {

    private final EmailService emailService;

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean existsUserByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public User saveUserInDB(User user) {
        return userRepository.save(user);
    }

    public User findUserByEmail(String email) {
        return findUserByEmail(
                email, InvalidCredentialsException::ifUserIsNotFounded);
    }

    public User findUserByEmail(
            String email,
            Supplier<RuntimeException> exceptionSupplier
    ) {

        return userRepository.findByEmail(email)
                .orElseThrow(exceptionSupplier);
    }

    public VerificationSentResponse sendCodeAndGetResponse(User user, String code) {
        emailService.sendVerificationCode(user, code);

        return generateVerificationSentResponse(user);
    }

    public VerificationSentResponse generateVerificationSentResponse(User user) {
        String email = user.getEmail();
        String message = String.format("Verification code sent to %s", email);

        return VerificationSentResponse.builder()
                .message(message)
                .email(email)
                .build();
    }

    public boolean isPasswordTryingCorrect(LoginRequest request, User user) {
        return passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );
    }

    public AuthResponse generateAuthResponse(User user) {
        String email = user.getEmail();
        Long userId = user.getId();
        Role role = user.getRole();

        String token = generateToken(email, userId, role);

        return AuthResponse.builder()
                .token(token)
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }

    public ValidateTokenResponse generateValidateTokenResponse(String token) {
        if (!validateToken(token)) {
            throw InvalidCredentialsException.byInvalidToken();
        }

        Long userId = extractUserIdFromToken(token);
        String email = extractEmailFromToken(token);
        Role role = extractRoleFromToken(token);

        return ValidateTokenResponse.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }

    private boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    private Long extractUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }

    private String extractEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }

    private Role extractRoleFromToken(String token) {
        return Role.valueOf(jwtUtil.extractRole(token));
    }

    private String generateToken(String email, Long userId, Role role) {
        return jwtUtil.generateToken(
                email,
                userId,
                role.name()
        );
    }

}
