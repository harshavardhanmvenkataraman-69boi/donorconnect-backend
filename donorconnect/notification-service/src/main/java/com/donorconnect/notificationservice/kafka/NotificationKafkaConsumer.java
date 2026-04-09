package com.donorconnect.notificationservice.kafka;

import com.donorconnect.notificationservice.entity.Notification;
import com.donorconnect.notificationservice.enums.NotificationCategory;
import com.donorconnect.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component @RequiredArgsConstructor @Slf4j
public class NotificationKafkaConsumer {
    private final NotificationRepository notificationRepo;

    /** Notify admins when a test result comes back reactive */
    @KafkaListener(topics = "blood.test.reactive", groupId = "notification-service-group")
    public void onTestReactive(Map<String, Object> event) {
        log.info("Notification: reactive test event received donationId={}", event.get("donationId"));
        saveNotification(1L, // Admin user
                "REACTIVE test result detected for donationId=" + event.get("donationId") + ". Quarantine initiated.",
                NotificationCategory.REACTIVE);
    }

    /** Notify relevant staff when a component is issued */
    @KafkaListener(topics = "transfusion.component.issued", groupId = "notification-service-group")
    public void onComponentIssued(Map<String, Object> event) {
        log.info("Notification: component issued event issueId={}", event.get("issueId"));
        saveNotification(1L,
                "Component " + event.get("componentId") + " issued to patient " + event.get("patientId") + ".",
                NotificationCategory.CROSSMATCH);
    }

    /** Notify relevant staff on safety alerts (reactions) */
    @KafkaListener(topics = "safety.alert", groupId = "notification-service-group")
    public void onSafetyAlert(Map<String, Object> event) {
        log.info("Notification: safety alert received reactionId={}", event.get("reactionId"));
        saveNotification(1L,
                "Adverse reaction reported for patientId=" + event.get("patientId") + ". Severity: " + event.get("severity"),
                NotificationCategory.REACTION);
    }

    /** Notify donors when flagged */
    @KafkaListener(topics = "donor.flagged", groupId = "notification-service-group")
    public void onDonorFlagged(Map<String, Object> event) {
        log.info("Notification: donor flagged donorId={}", event.get("donorId"));
        saveNotification(Long.valueOf(event.get("donorId").toString()),
                "Your donor profile has been flagged: " + event.get("reason"),
                NotificationCategory.REACTIVE);
    }

    private void saveNotification(Long userId, String message, NotificationCategory category) {
        notificationRepo.save(Notification.builder()
                .userId(userId)
                .message(message)
                .category(category)
                .build());
    }
}
