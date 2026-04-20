package com.donorconnect.bloodsupplyservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Get the current request from the request context
                ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (requestAttributes != null) {
                    HttpServletRequest request = requestAttributes.getRequest();

                    // Extract Authorization header from current request
                    String authorizationHeader = request.getHeader("Authorization");

                    // If Authorization header exists, add it to the Feign request
                    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        template.header("Authorization", authorizationHeader);
                    }
                }
            }
        };
    }
}
