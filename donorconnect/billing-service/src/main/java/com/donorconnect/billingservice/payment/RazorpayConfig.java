package com.donorconnect.billingservice.payment;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@Getter
public class RazorpayConfig {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    /**
     * Creates a singleton RazorpayClient bean that is reused across the app.
     * Spring injects this wherever {@code @Autowired RazorpayClient} is used.
     */
    @Bean
    public RazorpayClient razorpayClient() {
        try {
            log.info("Initialising Razorpay client with key: {}...", keyId.substring(0, 8));
            return new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            throw new IllegalStateException("Failed to initialise Razorpay client: " + e.getMessage(), e);
        }
    }
}
