package password_vault_backend.repository;

import password_vault_backend.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CredentialRepository extends JpaRepository<Credential, Long> {

    // Only fetch credentials belonging to a specific user
    List<Credential> findByUserId(Long userId);

    // Used to check a credential belongs to the requesting user before edit/delete
    Optional<Credential> findByIdAndUserId(Long id, Long userId);
}