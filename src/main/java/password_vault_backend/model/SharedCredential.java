package password_vault_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shared_credentials")
public class SharedCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credential_id")
    private Long credentialId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "shared_with_email")
    private String sharedWithEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // null = never expires
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private boolean revoked = false;

    public SharedCredential() {}

    public SharedCredential(Long credentialId, Long ownerId, String sharedWithEmail, LocalDateTime expiresAt) {
        this.credentialId = credentialId;
        this.ownerId = ownerId;
        this.sharedWithEmail = sharedWithEmail;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    // A share is usable if it hasn't been revoked and hasn't expired
    public boolean isActive() {
        if (revoked) return false;
        if (expiresAt == null) return true;
        return LocalDateTime.now().isBefore(expiresAt);
    }

    public Long getId() { return id; }
    public Long getCredentialId() { return credentialId; }
    public Long getOwnerId() { return ownerId; }
    public String getSharedWithEmail() { return sharedWithEmail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}