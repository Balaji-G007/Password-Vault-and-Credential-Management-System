package password_vault_backend.controller;

import password_vault_backend.model.*;
import password_vault_backend.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/security-dashboard")
public class SecurityDashboardController {

    @Autowired private LoginLogRepository loginLogRepository;
    @Autowired private SuspiciousActivityRepository suspiciousActivityRepository;
    @Autowired private SecurityAlertRepository securityAlertRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    private String getCurrentUserEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // GET /api/security-dashboard/summary - everything the dashboard needs, in one call
    @GetMapping("/summary")
    public ResponseEntity<?> summary() {
        String email = getCurrentUserEmail();

        List<LoginLog> logins = loginLogRepository.findByEmailOrderByAttemptedAtDesc(email);
        List<SuspiciousActivity> suspicious = suspiciousActivityRepository.findByUserEmailOrderByDetectedAtDesc(email);
        List<SecurityAlert> alerts = securityAlertRepository.findByUserEmailOrderByCreatedAtDesc(email);
        List<AuditLog> auditLogs = auditLogRepository.findByEmailOrderByCreatedAtDesc(email);

        long totalLogins = logins.size();
        long successfulLogins = logins.stream().filter(LoginLog::isSuccess).count();
        long failedLogins = totalLogins - successfulLogins;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogins", totalLogins);
        stats.put("successfulLogins", successfulLogins);
        stats.put("failedLogins", failedLogins);
        stats.put("suspiciousActivityCount", suspicious.size());
        stats.put("alertCount", alerts.size());
        stats.put("unreadAlertCount", alerts.stream().filter(a -> "UNREAD".equals(a.getStatus())).count());

        Map<String, Object> response = new HashMap<>();
        response.put("stats", stats);
        response.put("recentLogins", logins.stream().limit(5).collect(Collectors.toList()));
        response.put("recentSuspiciousActivity", suspicious.stream().limit(5).collect(Collectors.toList()));
        response.put("recentAlerts", alerts.stream().limit(5).collect(Collectors.toList()));
        response.put("recentAuditLogs", auditLogs.stream().limit(10).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }
}