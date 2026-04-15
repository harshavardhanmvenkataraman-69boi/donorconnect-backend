////package apiGateway.Config;
////
////
////import apiGateway.filter.JwtAuthenticationFilter;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.cloud.gateway.route.RouteLocator;
////import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////
////@Configuration
////public class GatewayConfig {
////
////    @Autowired
////    private JwtAuthenticationFilter jwtAuthenticationFilter;
////
////    @Bean
////    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
////        return builder.routes()
////
////                // IAM - Public auth endpoints (no JWT filter)
////                .route("iam-auth-public", r -> r
////                        .path("/api/v1/auth/setup-admin", "/api/v1/auth/login",
////                                "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password",
////                                "/api/v1/auth/validate-reset-token/**")
////                        .uri("lb://iam-service"))
////
////                // IAM - Protected auth endpoints (JWT filter applied)
////                .route("iam-auth-protected", r -> r
////                        .path("/api/v1/auth/logout")
////                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
////                        .uri("lb://iam-service"))
////
////                // IAM - User endpoints (JWT required)
////                .route("iam-users", r -> r
////                        .path("api/v1/users/**")
////                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
////                        .uri("lb://iam-service"))
////
////                // IAM - Admin endpoints (JWT required)
////                .route("iam-admin", r -> r
////                        .path("/admin/**")
////                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
////                        .uri("lb://iam-service"))
////
////                // Future: Add other service routes here
////                // .route("project-service", r -> r
////                //         .path("/projects/**")
////                //         .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
////                //         .uri("lb://project-service"))
////
////                .build();
////    }
////}
//
//
//
//package apiGateway.Config;
//
//import apiGateway.filter.JwtAuthenticationFilter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class GatewayConfig {
//
//    @Autowired
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//
//                // Auth-Service - Public auth endpoints (no JWT filter)
//                .route("auth-public", r -> r
//                        .path("/api/v1/auth/setup-admin", "/api/v1/auth/login",
//                                "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password")
//                        .uri("lb://auth-service"))
//
//                // Auth-Service - Protected auth endpoints (JWT filter applied)
//                .route("auth-protected", r -> r
//                        .path("/api/v1/auth/register", "/api/v1/auth/change-password")
//                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
//                        .uri("lb://auth-service"))
//
//                // Auth-Service - User management endpoints (JWT required)
//                .route("auth-users", r -> r
//                        .path("/api/v1/users/**")
//                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
//                        .uri("lb://auth-service"))
//
//                // Auth-Service - Admin endpoints (JWT required)
//                .route("auth-admin", r -> r
//                        .path("/admin/**")
//                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
//                        .uri("lb://auth-service"))
//
//                // Future: Add other service routes here
//                // .route("project-service", r -> r
//                //         .path("/projects/**")
//                //         .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
//                //         .uri("lb://project-service"))
//
//                .build();
//    }
//}
//


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
                // Public endpoints
                .route("auth-public", r -> r
                        .path("/api/v1/auth/setup-admin", "/api/v1/auth/login", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}"))
                        .uri("lb://auth-service"))

                // Protected endpoints
                .route("auth-protected", r -> r
                        .path("/api/v1/auth/register", "/api/v1/auth/change-password", "/api/v1/auth/logout")
                        .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)", "/api/v1/auth/${segment}")
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://auth-service"))

                // User/Admin Management
                .route("auth-users", r -> r
                        .path("/api/v1/users/**", "/admin/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://auth-service"))


                // --- 2. DONOR SERVICE ---
                .route("donor-service", r -> r
                        .path("/api/v1/donors/**", "/api/v1/screenings/**", "/api/v1/deferrals/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/(?<service>donors|screenings|deferrals)/(?<segment>.*)", "/api/v1/${service}/${segment}"))
                        .uri("lb://donor-service"))


                // --- 3. BLOOD SUPPLY SERVICE ---
                .route("blood-supply-service", r -> r
                        .path("/api/blood/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/blood/(?<segment>.*)", "/api/v1/blood/${segment}"))
                        .uri("lb://blood-supply-service"))


                // --- 4. TRANSFUSION SERVICE ---
                .route("transfusion-service", r -> r
                        .path("/api/transfusion/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/transfusion/(?<segment>.*)", "/api/v1/transfusion/${segment}"))
                        .uri("lb://transfusion-service"))


                // --- 5. SAFETY & BILLING & REPORTING ---
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


                // --- 6. UTILITY SERVICES (Notifications & Config) ---
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

                .build();
    }
}