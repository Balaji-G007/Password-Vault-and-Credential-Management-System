package password_vault_backend.repository;

import password_vault_backend.model.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    // All login attempts for a specific user (for their own activity view)
    List<LoginLog> findByEmailOrderByAttemptedAtDesc(String email);

    // All login attempts system-wide (for the security dashboard/monitoring)
    List<LoginLog> findAllByOrderByAttemptedAtDesc();
}