package apiGateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteValidator {

    // Endpoints that do NOT require JWT authentication
    private static final List<String> OPEN_ENDPOINTS = List.of(
            // Original /api/v1/auth/** patterns (for direct service access)
            "/api/v1/auth/setup-admin",
            "/api/v1/auth/login",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/validate-reset-token",

            // Frontend /api/auth/** patterns (before Gateway rewrites them)
            "/api/auth/setup-admin",
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",

            // Health and docs
            "/actuator/health"
    );

    public boolean isOpenEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return OPEN_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }
}

// Example: If your list has /api/auth/login and the user calls /api/auth/login?user=test,
// the path "starts with" the allowed string, so it returns TRUE.
