//package com.donorconnect.billingservice.kafka;
//
//
//import com.donorconnect.billingservice.enums.ChargeType;
//import com.donorconnect.billingservice.repository.BillingRefRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Map;
//
//@Component @RequiredArgsConstructor @Slf4j
//public class BillingKafkaConsumer {
//    private final BillingRefRepository billingRepo;
//
//    /**
//     * Consumes ComponentIssuedEvent from transfusion-service.
//     * Auto-generates a BillingRef for every successfully issued blood component.
//     */
//    @KafkaListener(topics = "transfusion.component.issued", groupId = "billing-service-group")
//    @Transactional
//    public void onComponentIssued(Map<String, Object> event) {
//        log.info("Billing service received ComponentIssuedEvent: {}", event);
//        Long issueId = event.get("issueId") != null
//                ? Long.valueOf(event.get("issueId").toString()) : null;
//        if (issueId == null) {
//            log.warn("issueId is null in ComponentIssuedEvent, skipping billing");
//            return;
//        }
//        Double charge = event.get("chargeAmount") != null
//                ? Double.parseDouble(event.get("chargeAmount").toString()) : 500.0;
//
//        com.donorconnect.billingservice.model.BillingRef billing = com.donorconnect.billingservice.model.BillingRef.builder()
//                .issueId(issueId)
//                .chargeAmount(BigDecimal.valueOf(charge))
//                .chargeType(ChargeType.PROCESSING)
//                .billingDate(LocalDate.now())
//                .build();
//        billingRepo.save(billing);
//        log.info("BillingRef created for issueId={}, amount={}", issueId, charge);
//    }
//}
