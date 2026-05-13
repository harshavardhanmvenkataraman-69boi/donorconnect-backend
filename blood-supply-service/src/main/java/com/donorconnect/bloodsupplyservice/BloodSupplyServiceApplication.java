package com.donorconnect.bloodsupplyservice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication @EnableDiscoveryClient @EnableFeignClients @EnableKafka @EnableScheduling
public class BloodSupplyServiceApplication {
    public static void main(String[] args) { SpringApplication.run(BloodSupplyServiceApplication.class, args); }
}
