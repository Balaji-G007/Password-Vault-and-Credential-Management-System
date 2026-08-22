package password_vault_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who did it
    private String email;

    // What happened, e.g. "CREDENTIAL_CREATED", "CREDENTIAL_SHARED", "TEAM_MEMBER_ADDED"
    private String action;

    // Human-readable detail, e.g. "Added credential for Netflix"
    private String details;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AuditLog() {}

    public AuditLog(String email, String action, String details) {
        this.email = email;
        this.action = action;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}