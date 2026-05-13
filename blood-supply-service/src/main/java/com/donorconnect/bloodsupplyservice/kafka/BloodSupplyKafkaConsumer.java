//package com.donorconnect.bloodsupplyservice.kafka;
//
//import com.donorconnect.bloodsupplyservice.entity.BloodComponent;
//import com.donorconnect.bloodsupplyservice.enums.ComponentStatus;
//import com.donorconnect.bloodsupplyservice.repository.BloodComponentRepository;
//import com.donorconnect.bloodsupplyservice.repository.QuarantineAction;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.transaction.annotation.Transactional;
//
//@BloodComponent
//@RequiredArgsConstructor @Slf4j
//public class BloodSupplyKafkaConsumer {
//    private final BloodComponentRepository componentRepository;
//    private final QuarantineAction inventoryBalanceRepository;
//
//    /**
//     * Listens to component-issued events from transfusion-service.
//     * Decrements inventory balance when a unit is issued.
//     */
//    @KafkaListener(topics = "transfusion.component.issued", groupId = "blood-supply-service-group")
//    @Transactional
//    public void onComponentIssued(ComponentIssuedEvent event) {
//        log.info("BloodSupply consuming ComponentIssuedEvent: componentId={}", event.getComponentId());
//        componentRepository.findById(event.getComponentId()).ifPresent(comp -> {
//            comp.setStatus(ComponentStatus.ISSUED);
//            componentRepository.save(comp);
//        });
//        inventoryBalanceRepository.findByComponentId(event.getComponentId()).ifPresent(bal -> {
//            bal.setQuantity(Math.max(0, bal.getQuantity() - 1));
//            inventoryBalanceRepository.save(bal);
//            log.info("Inventory decremented for componentId={}", event.getComponentId());
//        });
//    }
//
//    /**
//     * Listens to reactive test results from our own service (self-consumption pattern).
//     * Quarantines components linked to the reactive donation.
//     */
//    @KafkaListener(topics = "blood.test.reactive", groupId = "blood-supply-quarantine-group")
//    @Transactional
//    public void onTestReactive(TestResultReactiveEvent event) {
//        log.info("Quarantining components for donationId={}", event.getDonationId());
//        componentRepository.findByDonationId(event.getDonationId()).forEach(comp -> {
//            if (comp.getStatus() == ComponentStatus.AVAILABLE) {
//                comp.setStatus(ComponentStatus.QUARANTINE);
//                componentRepository.save(comp);
//                log.info("Component {} quarantined due to reactive test", comp.getComponentId());
//            }
//        });
//    }
//}
