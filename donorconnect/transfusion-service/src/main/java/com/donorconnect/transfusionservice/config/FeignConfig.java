package com.donorconnect.transfusionservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(FeignConfig.class);

    @Bean
    public RequestInterceptor jwtFeignInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs == null) {
                log.warn(">>> FeignConfig: No request context found — JWT NOT forwarded");
                return;
            }

            String auth = attrs.getRequest().getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                log.info(">>> FeignConfig: Forwarding JWT to {}", template.url());
                template.header("Authorization", auth);
            } else {
                log.warn(">>> FeignConfig: Authorization header missing or malformed");
            }
        };
    }
}