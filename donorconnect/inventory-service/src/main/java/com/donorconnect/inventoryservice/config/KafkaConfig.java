package com.donorconnect.inventoryservice.config;

import org.apache.kafka.clients.admin.NewTopic;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    // Topics this service PRODUCES to
    @Bean
    public NewTopic lowStockTopic() {
        return TopicBuilder.name("inventory.low.stock")
                .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic expiryAlertTopic() {
        return TopicBuilder.name("inventory.expiry.alert")
                .partitions(1).replicas(1).build();
    }

    // Topics this service CONSUMES from (created by other services, declared here for safety)
    @Bean
    public NewTopic componentIssuedTopic() {
        return TopicBuilder.name("transfusion.component.issued")
                .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic testReactiveTopic() {
        return TopicBuilder.name("blood.test.reactive")
                .partitions(1).replicas(1).build();
    }
}
