package password_vault_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_credentials")
public class TeamCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "credential_id")
    private Long credentialId;

    @Column(name = "shared_by")
    private Long sharedBy;

    @Column(name = "shared_at")
    private LocalDateTime sharedAt;

    public TeamCredential() {}

    public TeamCredential(Long teamId, Long credentialId, Long sharedBy) {
        this.teamId = teamId;
        this.credentialId = credentialId;
        this.sharedBy = sharedBy;
        this.sharedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getTeamId() { return teamId; }
    public Long getCredentialId() { return credentialId; }
    public Long getSharedBy() { return sharedBy; }
    public LocalDateTime getSharedAt() { return sharedAt; }
}