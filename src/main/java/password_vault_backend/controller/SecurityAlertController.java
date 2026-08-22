package password_vault_backend.controller;

import password_vault_backend.model.SecurityAlert;
import password_vault_backend.repository.SecurityAlertRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security-alerts")
public class SecurityAlertController {

    @Autowired private SecurityAlertRepository securityAlertRepository;

    @GetMapping("/me")
    public ResponseEntity<?> myAlerts() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<SecurityAlert> alerts = securityAlertRepository.findByUserEmailOrderByCreatedAtDesc(email);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/all")
    public ResponseEntity<?> allAlerts() {
        return ResponseEntity.ok(securityAlertRepository.findAllByOrderByCreatedAtDesc());
    }
}