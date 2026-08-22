package password_vault_backend.repository;

import password_vault_backend.model.SuspiciousActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuspiciousActivityRepository extends JpaRepository<SuspiciousActivity, Long> {
    List<SuspiciousActivity> findByUserEmailOrderByDetectedAtDesc(String userEmail);
    List<SuspiciousActivity> findAllByOrderByDetectedAtDesc();
}