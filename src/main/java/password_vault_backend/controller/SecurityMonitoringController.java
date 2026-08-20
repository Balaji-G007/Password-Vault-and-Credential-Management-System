package password_vault_backend.controller;

import password_vault_backend.Service.SuspiciousActivityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityMonitoringController {

    @Autowired private SuspiciousActivityService suspiciousActivityService;

    // GET /api/security/suspicious-activity
    // Returns emails with 3+ failed login attempts in the last 15 minutes
    @GetMapping("/suspicious-activity")
    public ResponseEntity<?> suspiciousActivity() {
        List<Map<String, Object>> flags = suspiciousActivityService.findSuspiciousActivity();
        return ResponseEntity.ok(flags);
    }
}