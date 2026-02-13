package dot.liberty.auth.service;

import dot.liberty.auth.client.NotificationServiceClient;
import dot.liberty.auth.client.dto.SendEmailRequest;
import dot.liberty.auth.dto.request.*;
import dot.liberty.auth.dto.response.AuthResponse;
import dot.liberty.auth.dto.response.ValidateTokenResponse;
import dot.liberty.auth.dto.response.VerificationSentResponse;
import dot.liberty.auth.entity.Role;
import dot.liberty.auth.entity.User;
import dot.liberty.auth.exception.*;
import dot.liberty.auth.repository.UserRepository;
import dot.liberty.auth.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final NotificationServiceClient notificationServiceClient;
    private final Random random = new Random();

    @Transactional
    public VerificationSentResponse register(RegisterRequest request) throws EmailAlreadyExistsException {
        String email = request.getEmail();

        if (userRepository.existsByEmail(email))
            throw new EmailAlreadyExistsException(email);

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        String code = generateVerificationCode();

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(15);

        Role role = request.getRole();

        User user = User.builder()
                .email(email)
                .password(hashedPassword)
                .role(role)
                .isEmailVerified(false)
                .verificationCode(code)
                .verificationCodeExpiresAt(expiresAt)
                .build();

        User savedUser = userRepository.save(user);

        return sendCodeAndGetResponse(savedUser, code);
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        User user = findUserAndThrowExceptionIfSearchingIsFailed(request.getEmail());

        if (user.getIsEmailVerified()) {
            throw new EmailAlreadyVerifiedException();
        }

        String code = request.getCode();

        if (user.getVerificationCode() == null
                || !user.getVerificationCode().equals(code)) {
            throw new InvalidVerificationCodeException();
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeExpiredException();
        }

        user.setIsEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);

        userRepository.save(user);

        return generateAuthResponse(user);
    }

    public VerificationSentResponse resendVerificationCode(ResendVerificationRequest request) {
        User user = findUserAndThrowExceptionIfSearchingIsFailed(request.getEmail());

        if (user.getIsEmailVerified()) {
            throw new EmailAlreadyVerifiedException();
        }

        String newCode = generateVerificationCode();

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(15);

        userRepository.save(user);

        return sendCodeAndGetResponse(user, newCode);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) throws InvalidCredentialsException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::byInvalidCredentials);

        if (!isPasswordTryingCorrect(request, user)) {
            throw InvalidCredentialsException.byInvalidCredentials();
        }

        if (!user.getIsEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public ValidateTokenResponse validateToken(ValidateTokenRequest request)
            throws InvalidCredentialsException {
        String token = request.getToken();

        if (!jwtUtil.validateToken(token)) {
            throw InvalidCredentialsException.byInvalidToken();
        }

        Long userId = jwtUtil.extractUserId(token);
        String email = jwtUtil.extractEmail(token);

        Role role = Role.valueOf(
                jwtUtil.extractRole(token));

        return ValidateTokenResponse.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }

    private String generateVerificationCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    private VerificationSentResponse sendCodeAndGetResponse(User user, String code) {
        sendVerificationCode(user, code);

        return generateVerificationSentResponse(user);
    }

    private void sendVerificationCode(User user, String code) {
        String email = user.getEmail();

        String messageTextTemplate = """
                Your verification code is: %s
                
                This code expires in 15 minutes.""";

        String subject = "Verify your email - One Touch Delivery";
        String body = String.format(messageTextTemplate, code);

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to(email)
                .subject(subject)
                .body(body)
                .build();

        notificationServiceClient.sendEmail(emailRequest);
    }

    private VerificationSentResponse generateVerificationSentResponse(User user) {
        String email = user.getEmail();
        String message = String.format("Verification code sent to %s", email);

        return VerificationSentResponse.builder()
                .message(message)
                .email(email)
                .build();
    }

    private User findUserAndThrowExceptionIfSearchingIsFailed(String email) {
        return findUserAndThrowExceptionIfSearchingIsFailed(
                email, InvalidCredentialsException::ifUserIsNotFounded);
    }

    private User findUserAndThrowExceptionIfSearchingIsFailed(
            String email, Supplier<RuntimeException> exceptionSupplier) {

        return userRepository.findByEmail(email)
                .orElseThrow(exceptionSupplier);
    }

    private boolean isPasswordTryingCorrect(LoginRequest request, User user) {
        return passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );
    }

    private AuthResponse generateAuthResponse(User user) {
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

    private String generateToken(String email, Long userId, Role role) {
        return jwtUtil.generateToken(
                email,
                userId,
                role.name()
        );
    }

}
