package password_vault_backend.controller;

import password_vault_backend.model.Credential;
import password_vault_backend.model.LoginLog;
import password_vault_backend.model.User;
import password_vault_backend.repository.CredentialRepository;
import password_vault_backend.repository.LoginLogRepository;
import password_vault_backend.repository.UserRepository;
import password_vault_backend.security.EncryptionUtil;
import password_vault_backend.Util.PasswordStrengthChecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @Autowired private CredentialRepository credentialRepository;
    @Autowired private LoginLogRepository loginLogRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EncryptionUtil encryptionUtil;

    private String getCurrentUserEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Long getCurrentUserId() {
        User user = userRepository.findByEmail(getCurrentUserEmail());
        return user.getId();
    }

    // GET /api/reports/password-health
    @GetMapping("/password-health")
    public ResponseEntity<?> passwordHealth() {
        Long userId = getCurrentUserId();
        List<Credential> credentials = credentialRepository.findByUserId(userId);

        int strong = 0, medium = 0, weak = 0;

        for (Credential c : credentials) {
            String plainPassword = encryptionUtil.decrypt(c.getEncryptedPassword());
            PasswordStrengthChecker.Strength strength = PasswordStrengthChecker.check(plainPassword);

            switch (strength) {
                case STRONG, VERY_STRONG -> strong++;
                case MEDIUM -> medium++;
                case WEAK -> weak++;
            }
        }

        int total = credentials.size();
        double healthScore = total == 0 ? 0 : Math.round((strong * 100.0 / total) * 10) / 10.0;

        Map<String, Object> response = new HashMap<>();
        response.put("totalCredentials", total);
        response.put("strong", strong);
        response.put("medium", medium);
        response.put("weak", weak);
        response.put("healthScore", healthScore);

        return ResponseEntity.ok(response);
    }

    // GET /api/reports/login-activity
    @GetMapping("/login-activity")
    public ResponseEntity<?> loginActivity() {
        String email = getCurrentUserEmail();
        List<LoginLog> logs = loginLogRepository.findByEmailOrderByAttemptedAtDesc(email);

        long total = logs.size();
        long successful = logs.stream().filter(LoginLog::isSuccess).count();
        long failed = total - successful;

        Map<String, Object> response = new HashMap<>();
        response.put("totalAttempts", total);
        response.put("successfulLogins", successful);
        response.put("failedLogins", failed);
        response.put("recentActivity", logs.stream().limit(10).toList());

        return ResponseEntity.ok(response);
    }
}