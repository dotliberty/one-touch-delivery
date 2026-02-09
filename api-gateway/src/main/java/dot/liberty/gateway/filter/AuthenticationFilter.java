package dot.liberty.gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    public AuthenticationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);

        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return null;
    }

    private Mono<ValidateTokenResponse> validateToken(String token) {
        return webClientBuilder.build()
                .post()
                .uri("lb://auth-service/api/auth/validate")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(new ValidateTokenRequest(token))
                .retrieve()
                .bodyToMono(ValidateTokenResponse.class);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/")
            || path.startsWith("/actuator/");
    }

    public static class Config {

    }

    private static class ValidateTokenRequest {
        private String token;

        public ValidateTokenRequest(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    private static class ValidateTokenResponse {
        private Long userId;
        private String email;
        private String role;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

}
