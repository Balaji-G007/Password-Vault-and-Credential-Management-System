package password_vault_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_alerts")
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "alert_type")
    private String alertType; // e.g. "MULTIPLE_FAILED_LOGIN_ATTEMPTS"

    private String message;

    private String severity; // "LOW", "MEDIUM", "HIGH"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String status; // "UNREAD", "READ"

    public SecurityAlert() {}

    public SecurityAlert(String userEmail, String alertType, String message, String severity) {
        this.userEmail = userEmail;
        this.alertType = alertType;
        this.message = message;
        this.severity = severity;
        this.createdAt = LocalDateTime.now();
        this.status = "UNREAD";
    }

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public String getAlertType() { return alertType; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}