package password_vault_backend.repository;

import password_vault_backend.model.SecurityAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {
    List<SecurityAlert> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    List<SecurityAlert> findAllByOrderByCreatedAtDesc();
}