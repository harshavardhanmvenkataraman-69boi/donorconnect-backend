package com.donorconnect.notificationservice.service;
import com.donorconnect.notificationservice.entity.Notification;
import com.donorconnect.notificationservice.enums.NotificationStatus;
import com.donorconnect.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repo;
    public List<Notification> getByUser(Long userId) { return repo.findByUserId(userId); }
    public List<Notification> getUnreadByUser(Long userId) { return repo.findByUserIdAndStatus(userId, NotificationStatus.UNREAD); }

    @Transactional
    public Notification markRead(Long id) {
        Notification n = repo.findById(id).orElseThrow();
        n.setStatus(NotificationStatus.READ);
        return repo.save(n);
    }

    @Transactional
    public Notification dismiss(Long id) {
        Notification n = repo.findById(id).orElseThrow();
        n.setStatus(NotificationStatus.DISMISSED);
        return repo.save(n);
    }

    public Notification save(Notification notification) { return repo.save(notification); }
}
