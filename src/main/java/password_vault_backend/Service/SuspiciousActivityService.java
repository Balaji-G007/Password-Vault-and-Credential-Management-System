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

    private static final int FAILED_ATTEMPT_THRESHOLD = 3;
    private static final int WINDOW_MINUTES = 15;

    public void checkAndFlag(String email) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);

        List<LoginLog> recentFailures = loginLogRepository.findByEmailOrderByAttemptedAtDesc(email)
                .stream()
                .filter(log -> !log.isSuccess())
                .filter(log -> log.getAttemptedAt().isAfter(windowStart))
                .toList();

        if (recentFailures.size() < FAILED_ATTEMPT_THRESHOLD) {
            return;
        }

        List<SuspiciousActivity> existing = suspiciousActivityRepository.findByUserEmailOrderByDetectedAtDesc(email);
        boolean alreadyFlaggedRecently = !existing.isEmpty()
                && existing.get(0).getDetectedAt().isAfter(windowStart);

        if (alreadyFlaggedRecently) {
            return;
        }

        String description = recentFailures.size() + " failed login attempts within " + WINDOW_MINUTES + " minutes";

        SuspiciousActivity activity = new SuspiciousActivity(email, "MULTIPLE_FAILED_LOGINS", description);
        suspiciousActivityRepository.save(activity);

        SecurityAlert alert = new SecurityAlert(
                email,
                "MULTIPLE_FAILED_LOGIN_ATTEMPTS",
                "Multiple failed login attempts detected on your account.",
                "HIGH"
        );
        securityAlertRepository.save(alert);

        auditLogService.log(email, "SUSPICIOUS_ACTIVITY_DETECTED", description);
        auditLogService.log(email, "SECURITY_ALERT_CREATED", "Alert: Multiple Failed Login Attempts (HIGH)");
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