package password_vault_backend.Service;

import password_vault_backend.model.Notification;
import password_vault_backend.model.NotificationType;
import password_vault_backend.repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

    // ── Core CRUD ──────────────────────────────────────────────────

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    // ── Async core: save to DB + send email in one call ────────────

    @Async
    public void sendNotification(Long userId, NotificationType type, String title,
                                 String inAppMessage, String recipientEmail,
                                 Runnable emailSender) {
        // 1. Save the in-app notification
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(inAppMessage);
        notificationRepository.save(notification);

        // 2. Send the HTML email (credentials/passwords are NEVER passed here)
        try {
            emailSender.run();
        } catch (Exception e) {
            System.err.println("Failed to send notification email for type " + type + ": " + e.getMessage());
        }
    }

    // ── Event Helpers ──────────────────────────────────────────────

    /**
     * LOGIN_SUCCESS — triggered after a verified successful login.
     */
    @Async
    public void createLoginSuccessNotification(Long userId, String email,
                                               String deviceInfo, String ipAddress) {
        String timestamp = LocalDateTime.now().format(DISPLAY_FORMAT);

        String inAppMessage = "New login detected on your VaultKeep account.\n\n"
                + "Date & Time: " + timestamp + "\n"
                + "Device: " + (deviceInfo != null ? deviceInfo : "Unknown") + "\n"
                + "IP Address: " + (ipAddress != null ? ipAddress : "Unknown");

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(NotificationType.LOGIN_SUCCESS);
        notification.setTitle("New login detected");
        notification.setMessage(inAppMessage);
        notificationRepository.save(notification);

        try {
            emailService.sendLoginSuccessEmail(email, timestamp,
                    deviceInfo != null ? deviceInfo : "Unknown",
                    ipAddress != null ? ipAddress : "Unknown");
        } catch (Exception e) {
            System.err.println("Failed to send login success email: " + e.getMessage());
        }
    }

    /**
     * FAILED_LOGIN_ALERT — triggered when 3+ consecutive failed logins are detected.
     * Includes audit security event logging via SuspiciousActivityService.
     */
    @Async
    public void createFailedLoginAlertNotification(Long userId, String email,
                                                    int failedAttempts) {
        String timestamp = LocalDateTime.now().format(DISPLAY_FORMAT);

        String inAppMessage = failedAttempts + " failed login attempts were detected on your account. "
                + "Please verify your account security.\n\nDetected at: " + timestamp;

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(NotificationType.FAILED_LOGIN_ALERT);
        notification.setTitle("Multiple failed login attempts");
        notification.setMessage(inAppMessage);
        notificationRepository.save(notification);

        try {
            emailService.sendFailedLoginAlertEmail(email, failedAttempts, timestamp);
        } catch (Exception e) {
            System.err.println("Failed to send failed login alert email: " + e.getMessage());
        }
    }

    /**
     * CREDENTIAL_SHARED — sent to the recipient when a credential is shared with them.
     * NO password or credential value is included in the message.
     */
    @Async
    public void createCredentialSharedNotification(Long recipientUserId, String recipientEmail,
                                                    String senderName, String credentialName) {
        String inAppMessage = "A credential '" + credentialName
                + "' has been securely shared with you by " + senderName + ".";

        Notification notification = new Notification();
        notification.setUserId(recipientUserId);
        notification.setType(NotificationType.CREDENTIAL_SHARED);
        notification.setTitle("Credential shared with you");
        notification.setMessage(inAppMessage);
        notificationRepository.save(notification);

        try {
            emailService.sendCredentialSharedEmail(recipientEmail, senderName, credentialName);
        } catch (Exception e) {
            System.err.println("Failed to send credential shared email: " + e.getMessage());
        }
    }

    /**
     * PASSWORD_EXPIRING — alert when a stored password needs rotation.
     */
    @Async
    public void createPasswordExpiringNotification(Long userId, String email, String accountName) {
        String inAppMessage = "Your password for '" + accountName
                + "' needs to be updated. Please change it to maintain account security.";

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(NotificationType.PASSWORD_EXPIRING);
        notification.setTitle("Password needs to be updated");
        notification.setMessage(inAppMessage);
        notificationRepository.save(notification);

        try {
            emailService.sendPasswordExpiringEmail(email, accountName);
        } catch (Exception e) {
            System.err.println("Failed to send password expiring email: " + e.getMessage());
        }
    }

    /**
     * SUSPICIOUS_RISK — triggered on suspicious/risky activity detection.
     */
    @Async
    public void createSuspiciousRiskNotification(Long userId, String email,
                                                  String activityDescription) {
        String timestamp = LocalDateTime.now().format(DISPLAY_FORMAT);

        String inAppMessage = "Suspicious activity was detected on your VaultKeep account.\n\n"
                + "Details: " + activityDescription + "\n"
                + "Time: " + timestamp;

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(NotificationType.SUSPICIOUS_RISK);
        notification.setTitle("Suspicious activity detected");
        notification.setMessage(inAppMessage);
        notificationRepository.save(notification);

        try {
            emailService.sendSuspiciousRiskEmail(email, activityDescription, timestamp);
        } catch (Exception e) {
            System.err.println("Failed to send suspicious risk email: " + e.getMessage());
        }
    }
}
