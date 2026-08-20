package password_vault_backend.repository;

import password_vault_backend.model.TeamCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamCredentialRepository extends JpaRepository<TeamCredential, Long> {

    List<TeamCredential> findByTeamId(Long teamId);

    Optional<TeamCredential> findByIdAndTeamId(Long id, Long teamId);
}