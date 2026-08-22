package password_vault_backend.repository;

import password_vault_backend.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEmailOrderByCreatedAtDesc(String email);
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}