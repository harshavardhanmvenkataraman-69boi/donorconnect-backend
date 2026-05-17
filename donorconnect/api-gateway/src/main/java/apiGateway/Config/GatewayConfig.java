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

    // whenever a request comes to gateway, the gateway dont know where to send it.
    // it uses route locator to figure out the destination, security rules, and if any changes
    // need to be made in url
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
                        .path("/api/billing", "/api/billing/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("^/api/billing", "/api/v1/billing"))
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


                .build();
    }
}