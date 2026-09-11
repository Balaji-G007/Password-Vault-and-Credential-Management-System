package password_vault_backend.Service;

import password_vault_backend.model.LoginLog;
import password_vault_backend.model.SecurityAlert;
import password_vault_backend.model.SuspiciousActivity;
import password_vault_backend.repository.LoginLogRepository;
import password_vault_backend.repository.SecurityAlertRepository;
import password_vault_backend.repository.SuspiciousActivityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SuspiciousActivityService {

    @Autowired private LoginLogRepository loginLogRepository;
    @Autowired private SuspiciousActivityRepository suspiciousActivityRepository;
    @Autowired private SecurityAlertRepository securityAlertRepository;
    @Autowired private AuditLogService auditLogService;
    @Autowired private NotificationService notificationService;

    private static final int FAILED_ATTEMPT_THRESHOLD = 3;
    private static final int WINDOW_MINUTES = 15;

    /**
     * Check recent failed logins for the given email.
     * If threshold (3+) is reached: create SuspiciousActivity + SecurityAlert,
     * fire an audit security event, AND send a FAILED_LOGIN_ALERT notification.
     *
     * @param email  the user's email
     * @param userId the user's DB id (needed for notification creation)
     */
    public void checkAndFlag(String email, Long userId) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);

        List<LoginLog> recentFailures = loginLogRepository.findByEmailOrderByAttemptedAtDesc(email)
                .stream()
                .filter(log -> !log.isSuccess())
                .filter(log -> log.getAttemptedAt().isAfter(windowStart))
                .toList();

        if (recentFailures.size() < FAILED_ATTEMPT_THRESHOLD) {
            return;
        }

        int failedCount = recentFailures.size();
        String description = failedCount + " failed login attempts within " + WINDOW_MINUTES + " minutes";

        List<SuspiciousActivity> existing = suspiciousActivityRepository.findByUserEmailOrderByDetectedAtDesc(email);
        boolean alreadyFlaggedRecently = !existing.isEmpty()
                && existing.get(0).getDetectedAt().isAfter(windowStart);

        if (alreadyFlaggedRecently) {
            SuspiciousActivity current = existing.get(0);
            current.setDescription(description);
            current.setDetectedAt(LocalDateTime.now());
            suspiciousActivityRepository.save(current);

            // Refresh the most recent alert so it reflects the latest count
            List<SecurityAlert> recentAlerts = securityAlertRepository.findByUserEmailOrderByCreatedAtDesc(email);
            if (!recentAlerts.isEmpty() && recentAlerts.get(0).getUpdatedAt().isAfter(windowStart)) {
                SecurityAlert latestAlert = recentAlerts.get(0);
                latestAlert.setMessage("Multiple failed login attempts detected on your account (" + failedCount + " attempts).");
                latestAlert.setUpdatedAt(LocalDateTime.now());
                latestAlert.setStatus("UNREAD");
                securityAlertRepository.save(latestAlert);
            }

            auditLogService.log(email, "SUSPICIOUS_ACTIVITY_UPDATED", description);

            // Send FAILED_LOGIN_ALERT notification
            notificationService.createFailedLoginAlertNotification(userId, email, failedCount);
            return;
        }

        // First time reaching the threshold — create new records
        SuspiciousActivity activity = new SuspiciousActivity(email, "MULTIPLE_FAILED_LOGINS", description);
        suspiciousActivityRepository.save(activity);

        SecurityAlert alert = new SecurityAlert(
                email,
                "MULTIPLE_FAILED_LOGIN_ATTEMPTS",
                "Multiple failed login attempts detected on your account.",
                "HIGH"
        );
        securityAlertRepository.save(alert);

        // Audit security events
        auditLogService.log(email, "SUSPICIOUS_ACTIVITY_DETECTED", description);
        auditLogService.log(email, "SECURITY_ALERT_CREATED", "Alert: Multiple Failed Login Attempts (HIGH)");

        // Send FAILED_LOGIN_ALERT notification (in-app + email)
        notificationService.createFailedLoginAlertNotification(userId, email, failedCount);
    }

    public List<SuspiciousActivity> getRecentForUser(String email) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);
        return suspiciousActivityRepository.findByUserEmailOrderByDetectedAtDesc(email)
                .stream()
                .filter(a -> a.getDetectedAt().isAfter(windowStart))
                .toList();
    }

    public List<SuspiciousActivity> getAll() {
        return suspiciousActivityRepository.findAllByOrderByDetectedAtDesc();
    }
}
