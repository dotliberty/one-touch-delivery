package dot.liberty.auth.service;

import dot.liberty.auth.dto.request.*;
import dot.liberty.auth.dto.response.AuthResponse;
import dot.liberty.auth.dto.response.ValidateTokenResponse;
import dot.liberty.auth.dto.response.VerificationSentResponse;
import dot.liberty.auth.entity.Role;
import dot.liberty.auth.entity.User;
import dot.liberty.auth.exception.*;
import dot.liberty.auth.util.VerificationCodeUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    @Transactional
    public VerificationSentResponse register(RegisterRequest request) throws EmailAlreadyExistsException {
        String email = request.getEmail();

        if (userService.existsUserByEmail(email))
            throw new EmailAlreadyExistsException(email);

        String hashedPassword = userService.encodePassword(request.getPassword());
        String code = VerificationCodeUtils.generateVerificationCode();

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

        User savedUser = userService.saveUserInDB(user);

        return userService.sendCodeAndGetResponse(savedUser, code);
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        User user = userService.findUserByEmail(request.getEmail());

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

        userService.saveUserInDB(user);

        return userService.generateAuthResponse(user);
    }

    @Transactional
    public VerificationSentResponse resendVerificationCode(ResendVerificationRequest request) {
        User user = userService.findUserByEmail(request.getEmail());

        if (user.getIsEmailVerified()) {
            throw new EmailAlreadyVerifiedException();
        }

        String newCode = VerificationCodeUtils.generateVerificationCode();

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(15);

        user.setVerificationCode(newCode);
        user.setVerificationCodeExpiresAt(expiresAt);

        userService.saveUserInDB(user);

        return userService.sendCodeAndGetResponse(user, newCode);
    }

    public AuthResponse login(LoginRequest request) throws InvalidCredentialsException {
        User user = userService.findUserByEmail(request.getEmail(),
                InvalidCredentialsException::byInvalidCredentials);

        if (!userService.isPasswordTryingCorrect(request, user)) {
            throw InvalidCredentialsException.byInvalidCredentials();
        }

        if (!user.getIsEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        return userService.generateAuthResponse(user);
    }

    public ValidateTokenResponse validateToken(ValidateTokenRequest request)
            throws InvalidCredentialsException {
        String token = request.getToken();

        return userService.generateValidateTokenResponse(token);
    }

}
