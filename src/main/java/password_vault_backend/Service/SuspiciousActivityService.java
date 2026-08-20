package password_vault_backend.Service;

import password_vault_backend.model.LoginLog;
import password_vault_backend.repository.LoginLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SuspiciousActivityService {

    @Autowired private LoginLogRepository loginLogRepository;

    // How many failed attempts within the window counts as suspicious
    private static final int FAILED_ATTEMPT_THRESHOLD = 3;
    private static final int WINDOW_MINUTES = 15;

    // Returns one summary per email that currently looks suspicious
    public List<Map<String, Object>> findSuspiciousActivity() {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);
        List<LoginLog> recentLogs = loginLogRepository.findAllByOrderByAttemptedAtDesc();

        // Group recent failed attempts by email
        Map<String, List<LoginLog>> failuresByEmail = recentLogs.stream()
                .filter(log -> !log.isSuccess())
                .filter(log -> log.getAttemptedAt().isAfter(windowStart))
                .collect(Collectors.groupingBy(LoginLog::getEmail));

        return failuresByEmail.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= FAILED_ATTEMPT_THRESHOLD)
                .map(entry -> {
                    Map<String, Object> flag = new java.util.HashMap<>();
                    flag.put("email", entry.getKey());
                    flag.put("failedAttempts", entry.getValue().size());
                    flag.put("windowMinutes", WINDOW_MINUTES);
                    flag.put("reason", "Multiple failed login attempts in a short time");
                    flag.put("mostRecentAttempt", entry.getValue().get(0).getAttemptedAt());
                    return flag;
                })
                .collect(Collectors.toList());
    }
}