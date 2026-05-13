package com.donorconnect.notificationservice.service;

import com.donorconnect.notificationservice.dto.request.NotificationRequest;
import com.donorconnect.notificationservice.entity.Notification;
import com.donorconnect.notificationservice.enums.NotificationCategory;
import com.donorconnect.notificationservice.enums.NotificationStatus;
import com.donorconnect.notificationservice.exception.ResourceNotFoundException;
import com.donorconnect.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification create(NotificationRequest req) {
        log.info("Creating notification for userId={} category={}", req.getUserId(), req.getCategory());
        Notification n = Notification.builder()
                .userId(req.getUserId())
                .message(req.getMessage())
                .category(req.getCategory())
                .status(NotificationStatus.UNREAD)
                .createdDate(LocalDateTime.now())
                .build();
        Notification saved = notificationRepository.save(n);
        log.info("Notification created notificationId={}", saved.getNotificationId());
        return saved;
    }

    public void createSystemNotification(Long userId, String message, NotificationCategory category) {
        log.info("Creating system notification for userId={} category={}", userId, category);
        Notification n = Notification.builder()
                .userId(userId)
                .message(message)
                .category(category)
                .status(NotificationStatus.UNREAD)
                .createdDate(LocalDateTime.now())
                .build();
        notificationRepository.save(n);
    }

    public List<Notification> getForUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public Notification getById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
    }

    public List<Notification> getUnreadForUser(Long userId) {
        return notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    public List<Notification> getByCategory(NotificationCategory category) {
        return notificationRepository.findByCategory(category);
    }

    @Transactional
    public Notification markRead(Long id) {
        Notification n = getById(id);
        n.setStatus(NotificationStatus.READ);
        return notificationRepository.save(n);
    }

    @Transactional
    public Notification dismiss(Long id) {
        Notification n = getById(id);
        n.setStatus(NotificationStatus.DISMISSED);
        return notificationRepository.save(n);
    }

    @Transactional
    public void markAllReadForUser(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        unread.forEach(n -> n.setStatus(NotificationStatus.READ));
        notificationRepository.saveAll(unread);
        log.debug("Marked {} notifications as READ for userId={}", unread.size(), userId);
    }

    public void delete(Long id) {
        notificationRepository.deleteById(id);
        log.info("Deleted notificationId={}", id);
    }

    // Internal use: called by Kafka consumers
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    /**
     * Get count of unread notifications for a user.
     */
    public long getUnreadCount(Long userId) {
        List<Notification> unread = getUnreadForUser(userId);
        return unread.size();
    }

    /**
     * Get count of notifications by category.
     */
    public long getCategoryCount(NotificationCategory category) {
        return notificationRepository.findByCategory(category).size();
    }

    /**
     * Check if a notification exists.
     */
    public boolean exists(Long notificationId) {
        return notificationRepository.existsById(notificationId);
    }
}
