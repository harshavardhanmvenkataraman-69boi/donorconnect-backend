package com.donorconnect.notificationservice.kafka;

import com.donorconnect.notificationservice.entity.Notification;
import com.donorconnect.notificationservice.enums.NotificationCategory;
import com.donorconnect.notificationservice.enums.NotificationStatus;
import com.donorconnect.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Kafka Consumer for notification events from other DonorConnect services.
 *
 * Topic alignment (verified against producer constants in each service):
 *   blood.test.reactive          -> blood-supply-service BloodSupplyKafkaProducer.TEST_REACTIVE_TOPIC
 *   transfusion.component.issued -> transfusion-service  TransfusionKafkaProducer.COMPONENT_ISSUED_TOPIC
 *   safety.alert                 -> safety-service       SafetyKafkaProducer.SAFETY_ALERT_TOPIC
 *   donor.flagged                -> donor-service        (appointment/donor events)
 *   appointment.scheduled        -> donor-service        AppointmentService
 *   inventory.low.stock          -> inventory-service    InventoryKafkaProducer.LOW_STOCK_TOPIC
 *   inventory.expiry.alert       -> inventory-service    InventoryKafkaProducer.EXPIRY_ALERT_TOPIC
 *   component.recalled           -> blood-supply-service RecallService
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final NotificationRepository notificationRepo;

    // ─── BLOOD SUPPLY SERVICE EVENTS ──────────────────────────────────────────

    /**
     * Notify admins when a test result comes back reactive.
     * Producer: blood-supply-service → topic "blood.test.reactive"
     * Payload keys: donationId, testType, [adminId]
     */
    @KafkaListener(topics = "blood.test.reactive", groupId = "notification-service-group", concurrency = "3")
    public void onTestReactive(Map<String, Object> event) {
        try {
            log.info("Event received: blood.test.reactive | donationId={}", event.get("donationId"));
            saveNotification(
                    resolveRecipient(event, 1L),
                    buildMessage("REACTIVE test result detected for donationId",
                            String.valueOf(event.get("donationId")), "— Quarantine initiated."),
                    NotificationCategory.REACTIVE
            );
        } catch (Exception e) {
            log.error("Error processing blood.test.reactive: {}", event, e);
        }
    }

    // ─── TRANSFUSION SERVICE EVENTS ───────────────────────────────────────────

    /**
     * Notify staff when a blood component is issued to a patient.
     * Producer: transfusion-service → topic "transfusion.component.issued"
     * Payload keys: issueId, componentId, patientId, bloodGroup, rhFactor, [staffId]
     */
    @KafkaListener(topics = "transfusion.component.issued", groupId = "notification-service-group", concurrency = "3")
    public void onComponentIssued(Map<String, Object> event) {
        try {
            log.info("Event received: transfusion.component.issued | issueId={}", event.get("issueId"));
            saveNotification(
                    resolveRecipient(event, 1L),
                    buildMessage("Component", String.valueOf(event.get("componentId")),
                            "issued to patient", String.valueOf(event.get("patientId"))),
                    NotificationCategory.CROSSMATCH
            );
        } catch (Exception e) {
            log.error("Error processing transfusion.component.issued: {}", event, e);
        }
    }

    // ─── SAFETY SERVICE EVENTS ────────────────────────────────────────────────

    /**
     * Notify admins on adverse reaction safety alerts.
     * Producer: safety-service → topic "safety.alert"
     * Payload keys: reactionId, patientId, severity, [adminId]
     */
    @KafkaListener(topics = "safety.alert", groupId = "notification-service-group", concurrency = "3")
    public void onSafetyAlert(Map<String, Object> event) {
        try {
            log.info("Event received: safety.alert | reactionId={}", event.get("reactionId"));
            saveNotification(
                    resolveRecipient(event, 1L),
                    buildMessage("Adverse reaction reported for patientId",
                            String.valueOf(event.get("patientId")),
                            "— Severity:", String.valueOf(event.get("severity"))),
                    NotificationCategory.REACTION
            );
        } catch (Exception e) {
            log.error("Error processing safety.alert: {}", event, e);
        }
    }

    // ─── DONOR SERVICE EVENTS ─────────────────────────────────────────────────

    /**
     * Notify donors when their profile is flagged.
     * Producer: donor-service → topic "donor.flagged"
     * Payload keys: donorId, reason
     */
    @KafkaListener(topics = "donor.flagged", groupId = "notification-service-group", concurrency = "3")
    public void onDonorFlagged(Map<String, Object> event) {
        try {
            log.info("Event received: donor.flagged | donorId={}", event.get("donorId"));
            Long donorId = extractLong(event, "donorId");
            saveNotification(
                    donorId,
                    "Your donor profile has been flagged: " + event.get("reason"),
                    NotificationCategory.REACTIVE
            );
        } catch (Exception e) {
            log.error("Error processing donor.flagged: {}", event, e);
        }
    }

    /**
     * Notify donors about scheduled appointments.
     * Producer: donor-service → topic "appointment.scheduled"
     * Payload keys: appointmentId, donorId, appointmentDate, location
     */
    @KafkaListener(topics = "appointment.scheduled", groupId = "notification-service-group", concurrency = "2")
    public void onAppointmentScheduled(Map<String, Object> event) {
        try {
            log.info("Event received: appointment.scheduled | appointmentId={}", event.get("appointmentId"));
            saveNotification(
                    extractLong(event, "donorId"),
                    buildMessage("Appointment scheduled on", String.valueOf(event.get("appointmentDate")),
                            "at", String.valueOf(event.get("location"))),
                    NotificationCategory.APPOINTMENT
            );
        } catch (Exception e) {
            log.error("Error processing appointment.scheduled: {}", event, e);
        }
    }

    // ─── BLOOD SUPPLY SERVICE — RECALL ────────────────────────────────────────

    /**
     * Notify admins about component recalls.
     * Producer: blood-supply-service → topic "component.recalled"
     * Payload keys: recallId, reason, [adminId]
     */
    @KafkaListener(topics = "component.recalled", groupId = "notification-service-group", concurrency = "2")
    public void onComponentRecalled(Map<String, Object> event) {
        try {
            log.info("Event received: component.recalled | recallId={}", event.get("recallId"));
            saveNotification(
                    resolveRecipient(event, 1L),
                    "Component recall initiated: " + event.get("reason"),
                    NotificationCategory.RECALL
            );
        } catch (Exception e) {
            log.error("Error processing component.recalled: {}", event, e);
        }
    }

    // ─── INVENTORY SERVICE EVENTS ─────────────────────────────────────────────
    // NOTE: inventory-service publishes to "inventory.low.stock" and "inventory.expiry.alert"
    // (see InventoryKafkaProducer.LOW_STOCK_TOPIC / EXPIRY_ALERT_TOPIC).

    /**
     * Notify inventory staff about low stock.
     * Producer: inventory-service → topic "inventory.low.stock"
     * Payload keys: type, count, timestamp, [staffId]
     */
    @KafkaListener(topics = "inventory.low.stock", groupId = "notification-service-group", concurrency = "2")
    public void onStockLowAlert(Map<String, Object> event) {
        try {
            log.info("Event received: inventory.low.stock | count={}", event.get("count"));
            saveNotification(
                    resolveRecipient(event, 1L),
                    buildMessage("Low stock alert:", String.valueOf(event.get("count")),
                            "item(s) below threshold as of", String.valueOf(event.get("timestamp"))),
                    NotificationCategory.STOCK
            );
        } catch (Exception e) {
            log.error("Error processing inventory.low.stock: {}", event, e);
        }
    }

    /**
     * Notify inventory staff about upcoming component expiry.
     * Producer: inventory-service → topic "inventory.expiry.alert"
     * Payload keys: type, componentId, daysToExpire, timestamp, [staffId]
     */
    @KafkaListener(topics = "inventory.expiry.alert", groupId = "notification-service-group", concurrency = "2")
    public void onComponentExpiryAlert(Map<String, Object> event) {
        try {
            log.info("Event received: inventory.expiry.alert | componentId={}", event.get("componentId"));
            saveNotification(
                    resolveRecipient(event, 1L),
                    buildMessage("Component", String.valueOf(event.get("componentId")),
                            "expires in", String.valueOf(event.get("daysToExpire")), "day(s)"),
                    NotificationCategory.EXPIRY
            );
        } catch (Exception e) {
            log.error("Error processing inventory.expiry.alert: {}", event, e);
        }
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private void saveNotification(Long userId, String message, NotificationCategory category) {
        if (userId == null || message == null || category == null) {
            log.warn("Skipping notification: incomplete data | userId={} | category={}", userId, category);
            return;
        }
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .category(category)
                .status(NotificationStatus.UNREAD)
                .createdDate(LocalDateTime.now())
                .build();
        notificationRepo.save(notification);
        log.debug("Notification persisted | userId={} | category={} | id={}",
                userId, category, notification.getNotificationId());
    }

    private Long extractLong(Map<String, Object> event, String key) {
        try {
            Object value = event.get(key);
            if (value == null) return null;
            if (value instanceof Long l) return l;
            if (value instanceof Integer i) return i.longValue();
            return Long.valueOf(value.toString());
        } catch (Exception e) {
            log.warn("Failed to extract Long for key '{}': {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Attempt to resolve a specific recipient from the event (adminId/staffId/donorId),
     * or fall back to the provided default (typically system admin userId=1).
     */
    private Long resolveRecipient(Map<String, Object> event, Long defaultUserId) {
        if (event.containsKey("adminId"))  return extractLong(event, "adminId");
        if (event.containsKey("staffId"))  return extractLong(event, "staffId");
        if (event.containsKey("donorId"))  return extractLong(event, "donorId");
        return defaultUserId;
    }

    private String buildMessage(String... parts) {
        return String.join(" ", parts);
    }
}
