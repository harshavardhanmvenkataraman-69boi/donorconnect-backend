package com.donorconnect.notificationservice.controller;

import com.donorconnect.notificationservice.dto.request.NotificationRequest;
import com.donorconnect.notificationservice.dto.response.ApiResponse;
import com.donorconnect.notificationservice.enums.NotificationCategory;
import com.donorconnect.notificationservice.security.JwtAuthenticationFilter;
import com.donorconnect.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications and alerts")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create notification manually (admin only)")
    public ResponseEntity<ApiResponse<?>> create(@RequestBody NotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Notification created", notificationService.create(request)));
    }

    @GetMapping
    @Operation(summary = "Get own notifications")
    public ResponseEntity<ApiResponse<?>> getMine(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success(notificationService.getForUser(userId)));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<ApiResponse<?>> getUnread(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadForUser(userId)));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get count of unread notifications")
    public ResponseEntity<ApiResponse<?>> getUnreadCount(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", notificationService.getUnreadCount(userId)));
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long notificationId) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getById(notificationId)));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Filter notifications by category (admin only)")
    public ResponseEntity<ApiResponse<?>> getByCategory(@PathVariable NotificationCategory category) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getByCategory(category)));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<?>> markRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(ApiResponse.success("Marked as read", notificationService.markRead(notificationId)));
    }

    @PatchMapping("/{notificationId}/dismiss")
    @Operation(summary = "Dismiss notification")
    public ResponseEntity<ApiResponse<?>> dismiss(@PathVariable Long notificationId) {
        return ResponseEntity.ok(ApiResponse.success("Dismissed", notificationService.dismiss(notificationId)));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<?>> markAllRead(HttpServletRequest request) {
        Long userId = extractUserId(request);
        notificationService.markAllReadForUser(userId);
        return ResponseEntity.ok(ApiResponse.success("All marked as read", null));
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete notification (admin only)")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long notificationId) {
        notificationService.delete(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

    /**
     * The JWT filter stores the numeric userId (from the "userId" claim in the token) as a
     * request attribute. The JWT subject is the username/email — not a numeric ID — so we
     * must read the dedicated attribute rather than parse auth.getName().
     */
    private Long extractUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
        if (userId == null) {
            throw new IllegalStateException("userId not found in request — JWT filter may not have run");
        }
        return (Long) userId;
    }
}
