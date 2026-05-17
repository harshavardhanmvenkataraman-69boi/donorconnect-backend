//package com.donorconnect.safetyservice.kafka;
//
//import com.donorconnect.safetyservice.entity.LookbackTrace;
//import com.donorconnect.safetyservice.repository.LookbackTraceRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import java.time.LocalDate;
//import java.util.Map;
//
//@Component @RequiredArgsConstructor @Slf4j
//public class SafetyKafkaConsumer {
//    private final LookbackTraceRepository lookbackRepo;
//
//    /**
//     * Consumes TestResultReactiveEvent from blood-supply-service.
//     * Triggers a lookback trace to identify all patients who may have received
//     * components from the same reactive donation.
//     */
//    @KafkaListener(topics = "blood.test.reactive", groupId = "safety-service-group")
//    @Transactional
//    public void onTestReactive(Map<String, Object> event) {
//        log.info("Safety service received reactive test event: {}", event);
//        Long donationId = event.get("donationId") != null
//                ? Long.valueOf(event.get("donationId").toString()) : null;
//        if (donationId == null) return;
//
//        // Create a lookback trace for this donation
//        LookbackTrace trace = LookbackTrace.builder()
//                .donationId(donationId)
//                .traceDate(LocalDate.now())
//                .build();
//        lookbackRepo.save(trace);
//        log.info("LookbackTrace created for donationId={}", donationId);
//    }
//}
