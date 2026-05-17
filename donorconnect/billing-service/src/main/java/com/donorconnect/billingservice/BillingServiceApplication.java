package com.donorconnect.billingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Billing Service entry point.
 *
 * Per the design spec this is a reference-based service: it captures
 * billing entries linked to issue records and exposes export endpoints.
 * No payment processing, no message-broker integrations.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}
