package password_vault_backend.repository;

import password_vault_backend.model.SharedCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharedCredentialRepository extends JpaRepository<SharedCredential, Long> {

    // Credentials shared WITH this email (things I can view because someone shared them with me)
    List<SharedCredential> findBySharedWithEmail(String email);

    // Credentials I OWN and have shared out (for managing/revoking)
    List<SharedCredential> findByOwnerId(Long ownerId);

    Optional<SharedCredential> findByIdAndOwnerId(Long id, Long ownerId);
}