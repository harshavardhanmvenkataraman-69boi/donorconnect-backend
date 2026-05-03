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

                // auth-service routes
                // Public: Login and setup/reset logic (MUST BE FIRST - NO JWT)
                .route("auth-login", r -> r
                        .path("/api/auth/login")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}"))
                        .uri("lb://auth-service"))

                .route("auth-setup", r -> r
                        .path("/api/auth/setup-admin")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}"))
                        .uri("lb://auth-service"))

                .route("auth-forgot", r -> r
                        .path("/api/auth/forgot-password")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}"))
                        .uri("lb://auth-service"))

                .route("auth-reset", r -> r
                        .path("/api/auth/reset-password")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}"))
                        .uri("lb://auth-service"))

                // Protected: Registration, Logout, and Password Changes (REQUIRES JWT)
                .route("auth-protected", r -> r
                        .path("/api/auth/register", "/api/auth/change-password", "/api/auth/logout")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}")
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://auth-service"))

                // Admin & User Management & Audit Logs
                .route("auth-users", r -> r
                        .path("/api/v1/users/**", "/api/v1/audit-logs/**", "/admin/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://auth-service"))

                // donor-service routes
                .route("donor-service", r -> r
                        .path("/api/donors/**", "/api/screenings/**", "/api/deferrals/**", "/api/drives/**", "/api/appointments/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/(?<service>donors|screenings|deferrals|drives|appointments)(?<remaining>/?.*)", "/api/v1/${service}${remaining}"))
                        .uri("lb://donor-service"))

                // blood-supply-service routes
                .route("blood-supply-service", r -> r
                        .path("/api/donations/**", "/api/components/**", "/api/recalls/**", "/api/quarantine/**", "/api/disposal/**", "/api/test-results/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/(?<service>donations|components|recalls|quarantine|disposal|test-results)(?<remaining>/?.*)", "/api/v1/${service}${remaining}"))
                        .uri("lb://blood-supply-service"))

                // transfusion-service routes
                .route("transfusion-service", r -> r
                        .path("/api/transfusion/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/transfusion/(?<segment>.*)", "/transfusion/api/v1/${segment}"))
                        .uri("lb://transfusion-service"))

                // inventory-service routes
                .route("inventory-service", r -> r
                        .path("/api/inventory/**", "/api/stock-transactions/**", "/api/expiry-watch/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/(?<service>inventory|stock-transactions|expiry-watch)(?<remaining>/?.*)", "/api/v1/${service}${remaining}"))
                        .uri("lb://inventory-service"))

                // safety-service routes
                .route("safety-service", r -> r
                        .path("/api/safety/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/safety/(?<segment>.*)", "/api/v1/safety/${segment}"))
                        .uri("lb://safety-service"))

                // billing-service routes        
                .route("billing-service", r -> r
                        .path("/api/billing/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/billing/(?<segment>.*)", "/api/v1/billing/${segment}"))
                        .uri("lb://billing-service"))

                // reporting-service routes        
                .route("reporting-service", r -> r
                        .path("/api/reports/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/reports/(?<segment>.*)", "/api/v1/reports/${segment}"))
                        .uri("lb://reporting-service"))


                // notification-service routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/notifications/(?<segment>.*)", "/api/v1/notifications/${segment}"))
                        .uri("lb://notification-service"))

                // config-service routes        
                .route("config-service", r -> r
                        .path("/api/config/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/config/(?<segment>.*)", "/api/v1/config/${segment}"))
                        .uri("lb://config-service"))

                /*    // swagger-ui and api-docs (have to make changes)
                .route("api-docs-auth", r -> r
                        .path("/v1/api-docs/auth-service")
                        .filters(f -> f.rewritePath("/v1/api-docs/auth-service", "/v1/api-docs"))
                        .uri("lb://auth-service"))

                .route("api-docs-donor", r -> r
                        .path("/v1/api-docs/donor-service")
                        .filters(f -> f.rewritePath("/v1/api-docs/donor-service", "/v1/api-docs"))
                        .uri("lb://donor-service"))

                .route("api-docs-blood-supply", r -> r
                        .path("/v1/api-docs/blood-supply-service")
                        .filters(f -> f.rewritePath("/v1/api-docs/blood-supply-service", "/v1/api-docs"))
                        .uri("lb://blood-supply-service"))

                .route("api-docs-transfusion", r -> r
                        .path("/v1/api-docs/transfusion-service")
                        .filters(f -> f.rewritePath("/v1/api-docs/transfusion-service", "/v1/api-docs"))
                        .uri("lb://transfusion-service"))

                .route("api-docs-inventory", r -> r
                        .path("/v1/api-docs/inventory-service")
                        .filters(f -> f.rewritePath("/v1/api-docs/inventory-service", "/v1/api-docs"))
                        .uri("lb://inventory-service"))

                .route("api-docs-safety", r -> r
                        .path("/v1/api-docs/safety-service")
                        .filters(f -> f.rewritePath("/v1/api-docs/safety-service", "/v1/api-docs"))
                        .uri("lb://safety-service"))

                .route("api-docs-billing", r -> r
                        .path("/v1/api-docs/billing-service")
                        .filters(f -> f.rewritePath("/v1/api-docs/billing-service", "/v1/api-docs"))
                        .uri("lb://billing-service"))

                .route("api-docs-reporting", r -> r
                        .path("/v1/api-docs/reporting-service")
                        .filters(f -> f.rewritePath("/v1/api-docs/reporting-service", "/v1/api-docs"))
                        .uri("lb://reporting-service"))

                .route("api-docs-notification", r -> r
                        .path("/v1/api-docs/notification-service")
                        .filters(f -> f.rewritePath("/v1/api-docs/notification-service", "/v1/api-docs"))
                        .uri("lb://notification-service"))
                */
                .build();
    }
}