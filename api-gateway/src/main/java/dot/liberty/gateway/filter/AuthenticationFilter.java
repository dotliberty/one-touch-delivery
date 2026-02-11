package dot.liberty.gateway.filter;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
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
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (isPublicPath(request.getPath().toString())) {
                return chain.filter(exchange);
            }

            HttpHeaders headers = request.getHeaders();

            if (areHeadersNotContainBearerHeader(headers)) {
                return onError(exchange, "Missing authorization header");
            }

            String authHeader = headers.getFirst(
                    HttpHeaders.AUTHORIZATION);

            if (isAuthHeaderInvalid(authHeader)) {
                return onError(exchange, "Invalid authorization header");
            }

            String token = authHeader.substring(7);

            return validateToken(token)
                    .flatMap(validationResponse -> {
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", validationResponse.getUserId().toString())
                                .header("X-User-Email", validationResponse.getEmail())
                                .header("X-User-Role", validationResponse.getRole())
                                .build();

                        return chain.filter(
                                exchange.mutate()
                                        .request(modifiedRequest)
                                        .build());
                    })
                    .onErrorResume(error -> onError(
                            exchange, "Invalid or expired token"));
        };
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

    private boolean areHeadersNotContainBearerHeader(HttpHeaders headers) {
        return !headers.containsKey(HttpHeaders.AUTHORIZATION);
    }

    private boolean isAuthHeaderInvalid(String authHeader) {
        return authHeader == null || !authHeader.startsWith("Bearer ");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        response.getHeaders()
                .add(HttpHeaders.CONTENT_TYPE, "application/json");

        String errorBody = String.format("{\"error\":\"%s\"}", message);

        DataBuffer action = response.bufferFactory()
                .wrap(errorBody.getBytes());

        return response.writeWith(Mono.just(action));
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
