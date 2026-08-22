package password_vault_backend.Service;

import password_vault_backend.model.AuditLog;
import password_vault_backend.repository.AuditLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    @Autowired private AuditLogRepository auditLogRepository;

    public void log(String email, String action, String details) {
        auditLogRepository.save(new AuditLog(email, action, details));
    }
}