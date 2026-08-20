package password_vault_backend.controller;

import password_vault_backend.model.LoginLog;
import password_vault_backend.repository.LoginLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/login-logs")
public class LoginLogController {

    @Autowired private LoginLogRepository loginLogRepository;

    // GET /api/login-logs/me - the logged-in user's own login activity
    @GetMapping("/me")
    public ResponseEntity<?> myLogs() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<LoginLog> logs = loginLogRepository.findByEmailOrderByAttemptedAtDesc(email);
        return ResponseEntity.ok(logs);
    }

    // GET /api/login-logs/all - every login attempt system-wide (for security monitoring/dashboard)
    @GetMapping("/all")
    public ResponseEntity<?> allLogs() {
        List<LoginLog> logs = loginLogRepository.findAllByOrderByAttemptedAtDesc();
        return ResponseEntity.ok(logs);
    }
}