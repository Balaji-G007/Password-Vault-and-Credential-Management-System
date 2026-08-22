package password_vault_backend.controller;

import password_vault_backend.Service.SuspiciousActivityService;
import password_vault_backend.model.SuspiciousActivity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security")
public class SecurityMonitoringController {

    @Autowired private SuspiciousActivityService suspiciousActivityService;

    // GET /api/security/suspicious-activity - the logged-in user's own flagged activity
    @GetMapping("/suspicious-activity")
    public ResponseEntity<?> myFlags() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<SuspiciousActivity> flags = suspiciousActivityService.getRecentForUser(email);
        return ResponseEntity.ok(flags);
    }

    // GET /api/security/suspicious-activity/all - every flagged activity (admin/monitoring view)
    @GetMapping("/suspicious-activity/all")
    public ResponseEntity<?> allFlags() {
        List<SuspiciousActivity> flags = suspiciousActivityService.getAll();
        return ResponseEntity.ok(flags);
    }
}