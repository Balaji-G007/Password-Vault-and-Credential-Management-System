package password_vault_backend.controller;

import password_vault_backend.model.Notification;
import password_vault_backend.model.User;
import password_vault_backend.repository.UserRepository;
import password_vault_backend.Service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Resolve the current authenticated user's ID from the JWT token.
     */
    private Long getCurrentUserId() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(email);
        return user.getId();
    }

    /**
     * GET /api/notifications
     * Returns all notifications for the authenticated user, ordered by newest first.
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    /**
     * GET /api/notifications/unread-count
     * Returns the count of unread notifications for the authenticated user.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Long userId = getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * PATCH /api/notifications/{id}/read
     * Marks a single notification as read.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read."));
    }

    /**
     * PATCH /api/notifications/read-all
     * Marks all notifications for the authenticated user as read.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read."));
    }
}
