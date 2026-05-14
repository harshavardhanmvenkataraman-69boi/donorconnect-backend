package apiGateway.Config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    // which origins are allowed to acceess your API
    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8001"
    };


    @Bean
    public CorsWebFilter corsWebFilter() { // checking for cors permission
        CorsConfiguration config = new CorsConfiguration(); // a container of security rules
        config.setAllowedOrigins(Arrays.asList(ALLOWED_ORIGINS));
        //OPTIONS -> Pre flight request, browser sends before sending the actual data
        // are you going to allow a react app from localhost 3000 to talk to you
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        //  allow all headers, you can specify which headers you want to allow
        // headers -> (Content-Type: Telling the server "I'm sending JSON.)
        //         -> (Authorization: Carrying the JWT Token so the server knows who you are)
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        // it means a browser doesn't have to send a pre flight request for every single api call you make in this 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // this applies these rules to every single endpoint in the gateway
        return new CorsWebFilter(source);
    }


    // Think of a Mono as a Promise (like in JavaScript) or a "Future Voucher.
    //It represents a data stream that will eventually result in 0 or 1 item
// Mono<Void> simply means: "I will finish this job, and when I'm done, I'll send a signal that the task is complete.
    // Mono.fromRunnable(...): This wraps your header-cleaning logic into a new Mono.
    // It allows the cleanup code to run in the same "reactive" flow.
    @Bean
    public GlobalFilter stripDownstreamCorsHeadersFilter() {
        return new GlobalFilter() {
            @Override
            // container for current request and response
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // it tells gateway to wait for the backend to finish and sends its response back.
                // only then run the code inside brackets
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    HttpHeaders headers = response.getHeaders();

                    // Remove duplicate CORS headers from downstream, keep only gateway's
                    // if your gateway send a CORS header and your microservice is also sending the same CORS header,
                    // browser receives two copies so it will call the following methods to make sure only one header exists
                    dedupeHeader(headers, HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
                    dedupeHeader(headers, HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
                    dedupeHeader(headers, HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
                    dedupeHeader(headers, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
                    dedupeHeader(headers, HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
                    dedupeHeader(headers, HttpHeaders.ACCESS_CONTROL_MAX_AGE);
                }));
            }

            private void dedupeHeader(HttpHeaders headers, String name) {
                var values = headers.get(name); // it checks how many values exist for a header
                if (values != null && values.size() > 1) {
                    headers.set(name, values.get(0)); // wipes all the headers and keeps only first one
                }
            }
        };
    }
}


//Your Gateway is configured to send CORS headers (to allow your React app to talk to it).
//Sometimes, your Auth Service is also configured to send CORS headers.
//When the response comes back to the browser, it might have two of every header:
//Access-Control-Allow-Origin: http://localhost:5173
//Access-Control-Allow-Origin: http://localhost:5173
//The Problem: Browsers hate this. If they see a duplicate CORS header,
// they consider it a security risk and block the whole request. Dedupe ensures that
// if there are two or more identical headers, we "wipe" the extras and keep only one