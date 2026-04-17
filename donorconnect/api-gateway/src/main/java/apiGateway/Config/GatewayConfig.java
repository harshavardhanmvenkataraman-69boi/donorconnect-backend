package apiGateway.Config;

import apiGateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // --- 1. AUTH SERVICE (IAM) ---
                // Public: Only login and setup/reset logic
                .route("auth-public", r -> r
                        .path("/api/auth/setup-admin", "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}"))
                        .uri("lb://auth-service"))

                // Protected: Registration, Logout, and Password Changes (REQUIRES JWT)
                .route("auth-protected", r -> r
                        .path("/api/auth/register", "/api/auth/change-password", "/api/auth/logout")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}")
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://auth-service"))

                // Admin & User Management
                .route("auth-users", r -> r
                        .path("/api/v1/users/**", "/admin/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://auth-service"))


                // --- 2. DONOR SERVICE ---
                .route("donor-service", r -> r
                        .path("/api/donors/**", "/api/screenings/**", "/api/deferrals/**", "/api/drives/**", "/api/appointments/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/(?<service>donors|screenings|deferrals|drives|appointments)(?<remaining>/?.*)", "/api/v1/${service}${remaining}"))
                        .uri("lb://donor-service"))


                // --- 3. BLOOD SUPPLY SERVICE ---
                .route("blood-supply-service", r -> r
                        .path("/api/donations/**", "/api/components/**", "/api/recalls/**", "/api/quarantine/**", "/api/disposal/**", "/api/test-results/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/(?<service>donations|components|recalls|quarantine|disposal|test-results)(?<remaining>/?.*)", "/api/v1/${service}${remaining}"))
                        .uri("lb://blood-supply-service"))


                // --- 4. TRANSFUSION SERVICE ---
                .route("transfusion-service", r -> r
                        .path("/api/transfusion/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/transfusion/(?<segment>.*)", "/api/v1/transfusion/${segment}"))
                        .uri("lb://transfusion-service"))


                // --- 5. INVENTORY SERVICE ---
                .route("inventory-service", r -> r
                        .path("/api/inventory/**", "/api/stock-transactions/**", "/api/expiry-watch/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/(?<service>inventory|stock-transactions|expiry-watch)(?<remaining>/?.*)", "/api/v1/${service}${remaining}"))
                        .uri("lb://inventory-service"))


                // --- 6. SAFETY, BILLING & REPORTING ---
                .route("safety-service", r -> r
                        .path("/api/safety/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/safety/(?<segment>.*)", "/api/v1/safety/${segment}"))
                        .uri("lb://safety-service"))

                .route("billing-service", r -> r
                        .path("/api/billing/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/billing/(?<segment>.*)", "/api/v1/billing/${segment}"))
                        .uri("lb://billing-service"))

                .route("reporting-service", r -> r
                        .path("/api/reports/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/reports/(?<segment>.*)", "/api/v1/reports/${segment}"))
                        .uri("lb://reporting-service"))


                // --- 7. UTILITY SERVICES ---
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/notifications/(?<segment>.*)", "/api/v1/notifications/${segment}"))
                        .uri("lb://notification-service"))

                .route("config-service", r -> r
                        .path("/api/config/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/config/(?<segment>.*)", "/api/v1/config/${segment}"))
                        .uri("lb://config-service"))

                // --- 8. SWAGGER / OPENAPI (PUBLIC) ---
                .route("openapi-docs", r -> r
                        .path("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**")
                        .uri("lb://auth-service"))

                .build();
    }
}