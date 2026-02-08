package dot.liberty.auth.service;

import dot.liberty.auth.dto.request.LoginRequest;
import dot.liberty.auth.dto.request.RegisterRequest;
import dot.liberty.auth.dto.request.ValidateTokenRequest;
import dot.liberty.auth.dto.response.AuthResponse;
import dot.liberty.auth.dto.response.ValidateTokenResponse;
import dot.liberty.auth.entity.Role;
import dot.liberty.auth.entity.User;
import dot.liberty.auth.exception.EmailAlreadyExistsException;
import dot.liberty.auth.exception.InvalidCredentialsException;
import dot.liberty.auth.repository.UserRepository;
import dot.liberty.auth.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) throws EmailAlreadyExistsException {
        String email = request.getEmail();

        if (userRepository.existsByEmail(email))
            throw new EmailAlreadyExistsException(email);

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        Role role = request.getRole();

        User user = User.builder()
                .email(email)
                .password(hashedPassword)
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        return generateAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) throws InvalidCredentialsException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::byInvalidCredentials);

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword()))
            throw InvalidCredentialsException.byInvalidCredentials();

        return generateAuthResponse(user);
    }

    @Transactional
    public ValidateTokenResponse validateToken(ValidateTokenRequest request)
            throws InvalidCredentialsException {
        String token = request.getToken();

        if (!jwtUtil.validateToken(token))
            throw InvalidCredentialsException.byInvalidCredentials();

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
