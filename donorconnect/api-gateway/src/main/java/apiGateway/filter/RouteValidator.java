package apiGateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteValidator {

    // Endpoints that do NOT require JWT authentication
    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/v1/auth/setup-admin",
            "/api/v1/auth/login",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/validate-reset-token",
            "/actuator/health",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v1/api-docs",
            "/api-docs",
            "/swagger-resources"
    );

    public boolean isOpenEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return OPEN_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }
}
