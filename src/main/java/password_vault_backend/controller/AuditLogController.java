package password_vault_backend.controller;

import password_vault_backend.model.AuditLog;
import password_vault_backend.repository.AuditLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired private AuditLogRepository auditLogRepository;

    @GetMapping("/me")
    public ResponseEntity<?> myLogs() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<AuditLog> logs = auditLogRepository.findByEmailOrderByCreatedAtDesc(email);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/all")
    public ResponseEntity<?> allLogs() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByCreatedAtDesc());
    }
}