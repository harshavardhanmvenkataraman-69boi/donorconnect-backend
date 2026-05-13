package com.donorconnect.notificationservice.repository;
import com.donorconnect.notificationservice.entity.Notification;
import com.donorconnect.notificationservice.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);
    List<Notification> findByCategory(NotificationCategory category);
}
