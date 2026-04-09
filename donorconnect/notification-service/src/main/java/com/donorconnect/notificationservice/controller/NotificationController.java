package com.donorconnect.notificationservice.controller;
import com.donorconnect.notificationservice.entity.Notification;
import com.donorconnect.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;
    @GetMapping("/user/{userId}") public ResponseEntity<List<Notification>> byUser(@PathVariable Long userId) { return ResponseEntity.ok(service.getByUser(userId)); }
    @GetMapping("/user/{userId}/unread") public ResponseEntity<List<Notification>> unread(@PathVariable Long userId) { return ResponseEntity.ok(service.getUnreadByUser(userId)); }
    @PutMapping("/{id}/read") public ResponseEntity<Notification> markRead(@PathVariable Long id) { return ResponseEntity.ok(service.markRead(id)); }
    @PutMapping("/{id}/dismiss") public ResponseEntity<Notification> dismiss(@PathVariable Long id) { return ResponseEntity.ok(service.dismiss(id)); }
    @PostMapping public ResponseEntity<Notification> create(@RequestBody Notification n) { return ResponseEntity.ok(service.save(n)); }
}
