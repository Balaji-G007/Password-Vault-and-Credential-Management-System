package password_vault_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "suspicious_activities")
public class SuspiciousActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "activity_type")
    private String activityType; // e.g. "MULTIPLE_FAILED_LOGINS"

    private String description;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    private String status; // "NEW", "REVIEWED", "RESOLVED"

    public SuspiciousActivity() {}

    public SuspiciousActivity(String userEmail, String activityType, String description) {
        this.userEmail = userEmail;
        this.activityType = activityType;
        this.description = description;
        this.detectedAt = LocalDateTime.now();
        this.status = "NEW";
    }

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public String getActivityType() { return activityType; }
    public String getDescription() { return description; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}